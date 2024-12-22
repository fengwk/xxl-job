package com.xxl.job.admin.service.impl;

import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.core.util.CookieUtil;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.core.util.JacksonUtil;
import com.xxl.job.admin.dao.XxlJobUserDao;
import com.xxl.job.core.biz.model.ReturnT;
import fun.fengwk.convention4j.api.result.Result;
import fun.fengwk.gateway.share.context.GatewayContext;
import fun.fengwk.upms.share.permission.client.UserPermissionFeignClient;
import fun.fengwk.upms.share.user.client.UserFeignClient;
import fun.fengwk.upms.share.user.model.UserDTO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.math.BigInteger;

/**
 * @author xuxueli 2019-05-04 22:13:264
 */
@Slf4j
@Service
public class LoginService {

    public static final String LOGIN_IDENTITY_KEY = "XXL_JOB_LOGIN_IDENTITY";

    @Resource
    private XxlJobUserDao xxlJobUserDao;
    @Resource
    private UserFeignClient userFeignClient;
    @Resource
    private UserPermissionFeignClient userPermissionFeignClient;
    @Resource
    private GatewayContext gatewayContext;

    @Value("${xxl.job.upms-permission-admin:xxl_job_admin}")
    private String upmsPermissionAdmin;
    @Value("${xxl.job.upms-permission-user:xxl_job_user}")
    private String upmsPermissionUser;


    // ---------------------- token tool ----------------------

    private String makeToken(XxlJobUser xxlJobUser){
        String tokenJson = JacksonUtil.writeValueAsString(xxlJobUser);
        String tokenHex = new BigInteger(tokenJson.getBytes()).toString(16);
        return tokenHex;
    }
    private XxlJobUser parseToken(String tokenHex){
        XxlJobUser xxlJobUser = null;
        if (tokenHex != null) {
            String tokenJson = new String(new BigInteger(tokenHex, 16).toByteArray());      // username_password(md5)
            xxlJobUser = JacksonUtil.readValue(tokenJson, XxlJobUser.class);
        }
        return xxlJobUser;
    }


    // ---------------------- login tool, with cookie and db ----------------------

    public ReturnT<String> login(HttpServletRequest request, HttpServletResponse response, String username, String password, boolean ifRemember){

        // param
        if (username==null || username.trim().length()==0 || password==null || password.trim().length()==0){
            return new ReturnT<String>(500, I18nUtil.getString("login_param_empty"));
        }

        // valid passowrd
        XxlJobUser xxlJobUser = xxlJobUserDao.loadByUserName(username);
        if (xxlJobUser == null) {
            return new ReturnT<String>(500, I18nUtil.getString("login_param_unvalid"));
        }
        String passwordMd5 = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!passwordMd5.equals(xxlJobUser.getPassword())) {
            return new ReturnT<String>(500, I18nUtil.getString("login_param_unvalid"));
        }

        String loginToken = makeToken(xxlJobUser);

        // do login
        CookieUtil.set(response, LOGIN_IDENTITY_KEY, loginToken, ifRemember);
        return ReturnT.SUCCESS;
    }

    /**
     * logout
     *
     * @param request
     * @param response
     */
    public ReturnT<String> logout(HttpServletRequest request, HttpServletResponse response){
        CookieUtil.remove(request, response, LOGIN_IDENTITY_KEY);
        return ReturnT.SUCCESS;
    }

    /**
     * logout
     *
     * @param request
     * @return
     */
    public XxlJobUser ifLogin(HttpServletRequest request, HttpServletResponse response){
        String namespace = gatewayContext.getAccessUserNamespace();
        Long userId = gatewayContext.getAccessUserId();
        if (!StringUtils.hasText(namespace) || userId == null) {
            return null;
        }

        Integer userRole = getUserRole(namespace, userId);
        if (userRole == null) {
            return null;
        }

        Result<UserDTO> userResult = userFeignClient.getUser(namespace, userId);
        if (!userResult.isSuccess()) {
            log.error("get login user failed, namespace: {}, userId: {}, userResult: {}",
                namespace, userId, userResult);
            return null;
        }

        UserDTO userDTO = userResult.getData();

        XxlJobUser xxlJobUser = new XxlJobUser();
        xxlJobUser.setId(userDTO.getUserId());
        xxlJobUser.setUsername(userDTO.getUsername());
        xxlJobUser.setRole(userRole);
        xxlJobUser.setPermission("");
        return xxlJobUser;
    }

    private Integer getUserRole(String namespace, Long userId) {
        Result<Boolean> userPermissionResult = userPermissionFeignClient.validatePermission(namespace, userId, upmsPermissionUser);
        if (userPermissionResult.isSuccess()) {
            if (userPermissionResult.getData()) {
                return 0;
            }
        } else {
            log.error("query user role failed, namespace: {}, userId: {}", namespace, userId);
        }

        Result<Boolean> adminPermissionResult = userPermissionFeignClient.validatePermission(namespace, userId, upmsPermissionAdmin);
        if (adminPermissionResult.isSuccess()) {
            if (adminPermissionResult.getData()) {
                return 1;
            }
        } else {
            log.error("query admin role failed, namespace: {}, userId: {}", namespace, userId);
        }

        return null;
    }

}
