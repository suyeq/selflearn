package com.suyeq.shop.controller;

import com.suyeq.shop.dto.Result;
import com.suyeq.shop.service.ShopService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author : denglinhai
 * @date : 18:03 2023/5/10
 */
@CrossOrigin
@RestController
public class ShopController {

    @Resource
    private ShopService shopService;

    @GetMapping("/{id}")
    public Result<String> selectShopDetails(@PathVariable("id") Long id) {
        shopService.selectUserDetails(id);
        return new Result<>(true);
    }
}
