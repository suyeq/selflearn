package com.suyeq.shop.dto;

import org.slf4j.MDC;

/**
 * @author : denglinhai
 * @date : 16:55 2023/5/11
 */
public class Result<T> {
    private String code;
    private String msg;
    private String traceId;
    private boolean success;
    private T data;

    public Result(boolean success) {
        this.traceId = MDC.get("traceId");
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
