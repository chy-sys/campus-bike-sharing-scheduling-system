package com.campus.bike.common.handler;

import com.campus.bike.common.R;
import com.campus.bike.common.ResultCode;
import com.campus.bike.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理：所有 Controller 抛出的异常统一转成 R 结构返回
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException e) {
        log.warn("业务异常：{}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常（@RequestBody + @Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return R.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 表单绑定校验异常
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return R.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(ResultCode.ERROR);
    }
}
