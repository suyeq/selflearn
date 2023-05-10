package com.suyeq.user.controller;

import com.suyeq.user.service.UserService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author : denglinhai
 * @date : 17:37 2023/5/10
 */
@CrossOrigin
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping(value = "/{id}")
    public String selectUserDetails(@PathVariable("id") Long id) {
        return userService.selectUserDetails(id);
    }
}
