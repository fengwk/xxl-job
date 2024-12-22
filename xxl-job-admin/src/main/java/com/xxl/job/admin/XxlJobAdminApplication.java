package com.xxl.job.admin;

import fun.fengwk.upms.share.oauth2.client.OAuth2FeignClient;
import fun.fengwk.upms.share.permission.client.UserPermissionFeignClient;
import fun.fengwk.upms.share.user.client.UserFeignClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author xuxueli 2018-10-28 00:38:13
 */
@EnableFeignClients(clients = {
    UserFeignClient.class, UserPermissionFeignClient.class, OAuth2FeignClient.class
})
@SpringBootApplication
public class XxlJobAdminApplication {

	public static void main(String[] args) {
        SpringApplication.run(XxlJobAdminApplication.class, args);
	}

}