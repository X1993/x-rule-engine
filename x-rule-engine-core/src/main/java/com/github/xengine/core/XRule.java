package com.github.xengine.core;

/**
 * 规则
 * @author X1993
 * @date 2023/2/10
 * @description
 */
public interface XRule<CONTENT extends XRuleContent> {

    /**
     * 规则名称
     * @return
     */
    default String name(){
        return toString();
    }

    /**
     * 规则执行超时毫秒数
     * @return
     * 小于等于零: 使用默认超时时间
     * @see XEngineProperties#getDefaultRuleExeTimeoutMS()
     * @see DefaultXNodeExecutor#xEngineProperties
     */
    default long timeoutMS(){
        return -1;
    }

    /**
     * 执行规则
     * @param content
     */
    void execute(CONTENT content) throws Exception;

}
