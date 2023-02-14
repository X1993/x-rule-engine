package com.github.xengine.core;

/**
 * 节点执行链配置异常
 * 1.起始节点有且只能有一个
 * 2.终止节点有且只能有一个
 * 3.节点之间不能构成环
 * @author wangjj7
 * @date 2023/2/14
 * @description
 */
public class XNodeException extends RuntimeException {

    public XNodeException() {
    }

    public XNodeException(String message) {
        super(message);
    }

    public XNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public XNodeException(Throwable cause) {
        super(cause);
    }

    public XNodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
