package com.github.xengine.core;

/**
 * 规则执行超时异常
 * @author X1993
 * @date 2023/2/24
 * @description
 */
public class XRuleTimeoutException extends RuntimeException{

    public XRuleTimeoutException() {
    }

    public XRuleTimeoutException(String message) {
        super(message);
    }

    public XRuleTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public XRuleTimeoutException(Throwable cause) {
        super(cause);
    }

    public XRuleTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
