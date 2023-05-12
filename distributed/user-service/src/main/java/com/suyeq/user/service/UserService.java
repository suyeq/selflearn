package com.suyeq.user.service;

import com.suyeq.user.conf.AntMqConf;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author : denglinhai
 * @date : 17:35 2023/5/10
 */
@Service
public class UserService {

    @Resource
    private AntMqConf mqConf;

    public String selectUserDetails(Long userId) {
        System.out.println("获取用户详情");
        System.out.println(mqConf.getServer());
        return userId.toString();
    }

}
