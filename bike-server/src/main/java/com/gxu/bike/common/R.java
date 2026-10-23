package com.gxu.bike.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体，与 Android 端约定的结构：{ code, msg, data }
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String msg;
    private T data;

    public R() {
    }

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(ResultCode.BAD_REQUEST.getCode(), msg, null);
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }

    /**
     * 是否成功，便于服务端内部判断
     */
    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }
}
