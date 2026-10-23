package com.gxu.bike.exception;

import com.gxu.bike.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常：可预期的错误（参数错误、数据不存在、状态不允许等）
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String msg) {
        super(msg);
        this.code = ResultCode.BAD_REQUEST.getCode();
    }

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BizException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }
}
