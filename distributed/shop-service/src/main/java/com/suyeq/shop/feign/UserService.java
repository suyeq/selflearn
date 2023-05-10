package com.suyeq.shop.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author : denglinhai
 * @date : 17:54 2023/5/10
 */
@Component
@FeignClient("userService")
@RequestMapping("/user")
public interface UserService {

    @GetMapping("/{id}")
    String selectUserDetails(@PathVariable("id") Long id);

}
