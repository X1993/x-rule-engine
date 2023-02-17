package com.github.xengine.core;

/**
 * 规则执行状态
 * @author X1993
 * @date 2023/2/10
 * @description
 */
public enum XRuleStatus {

    /**
     * 等待
     */
    WAIT,

    /**
     * 就绪
     */
    READY,

    /**
     * 执行中
     */
    EXECUTING,

    /**
     * 取消
     */
    CANCEL,

    /**
     * 异常结束
     */
    EXCEPTION,

    /**
     * 中断结束
     */
    INTERCEPT,

    /**
     * 正常结束
     */
    COMPLETE,

    ;

    public boolean isFinal(){
        return this == CANCEL || this == EXCEPTION || this == INTERCEPT || this == COMPLETE;
    }

}
