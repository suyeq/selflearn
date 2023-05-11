package com.suyeq.shop.service;

import com.suyeq.shop.feign.UserService;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author : denglinhai
 * @date : 17:53 2023/5/10
 */
@Service
public class ShopService {
    private final static Logger logger = LoggerFactory.getLogger(ShopService.class);

    @Resource
    private UserService userService;

    public String selectUserDetails(Long id) {
        String traceId = TraceContext.traceId();
        logger.info("test info log...., {}", traceId);
        logger.error("test error log....");
        return userService.selectUserDetails(id);
    }
}
