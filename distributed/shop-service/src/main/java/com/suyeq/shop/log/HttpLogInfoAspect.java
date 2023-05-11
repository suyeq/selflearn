package com.suyeq.shop.log;

import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author : denglinhai
 * @date : 14:44 2022/9/7
 */
@Aspect
@Component
public class HttpLogInfoAspect {
    private final static Logger logger = LoggerFactory.getLogger(HttpLogInfoAspect.class);

    // 日志的切入点
    @Pointcut("execution(public * com.suyeq.shop.controller..*.*(..))")
    public void httpLog() {
    }

    // 打印请求日志
    @Before("httpLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        // 开始打印请求日志
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        MDC.put("traceId",  TraceContext.traceId());

        // 打印请求相关参数
        logger.info("request url:[{}], httpMethod:[{}], " + "remoteAddr:[{}]", request.getRequestURI(),
                request.getMethod(), request.getRemoteAddr());
    }

    // 打印此次调用耗时以及返回结果
    @Around("httpLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        // 打印出参与耗时
        logger.info("timeCost:[{}] ms", System.currentTimeMillis() - startTime);
        return result;
    }
}
