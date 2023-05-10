package com.suyeq.user.service;

import org.springframework.stereotype.Service;

/**
 * @author : denglinhai
 * @date : 17:35 2023/5/10
 */
@Service
public class UserService {

    public String selectUserDetails(Long userId) {
        System.out.println("获取用户详情");
        return userId.toString();
    }

}
