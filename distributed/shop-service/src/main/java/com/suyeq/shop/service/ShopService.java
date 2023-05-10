package com.suyeq.shop.service;

import com.suyeq.shop.feign.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author : denglinhai
 * @date : 17:53 2023/5/10
 */
@Service
public class ShopService {

    @Resource
    private UserService userService;

    public String selectUserDetails(Long id) {
        return userService.selectUserDetails(id);
    }
}
