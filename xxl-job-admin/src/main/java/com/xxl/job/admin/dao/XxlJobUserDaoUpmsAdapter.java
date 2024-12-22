package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.XxlJobUser;
import fun.fengwk.convention4j.common.json.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author fengwk
 */
@Slf4j
@AllArgsConstructor
@Component
public class XxlJobUserDaoUpmsAdapter implements XxlJobUserDao {

    @Override
    public List<XxlJobUser> pageList(int offset, int pagesize, String username, int role) {
        return Collections.emptyList();
    }

    @Override
    public int pageListCount(int offset, int pagesize, String username, int role) {
        return 0;
    }

    @Override
    public XxlJobUser loadByUserName(String username) {
        throw new UnsupportedOperationException("Not support loadByUserName");
    }

    @Override
    public int save(XxlJobUser xxlJobUser) {
        log.error("Please go to the UPMS system to save the user, xxlJobUser: {}", JsonUtils.toJson(xxlJobUser));
        throw new UnsupportedOperationException("Please go to the UPMS system to save the user");
    }

    @Override
    public int update(XxlJobUser xxlJobUser) {
        log.error("Please go to the UPMS system to update the user, xxlJobUser: {}", JsonUtils.toJson(xxlJobUser));
        throw new UnsupportedOperationException("Please go to the UPMS system to update the user");
    }

    @Override
    public int delete(long id) {
        log.error("Please go to the UPMS system to delete the user, id: {}", id);
        throw new UnsupportedOperationException("Please go to the UPMS system to delete the user");
    }

}
