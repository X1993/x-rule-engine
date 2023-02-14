package com.github.xengine.core;

/**
 * 规则
 * @author wangjj7
 * @date 2023/2/10
 * @description
 */
public interface XRule<CONTENT extends XRuleContent<CONTENT>> {

    /**
     * 规则名称
     * @return
     */
    String name();

    /**
     * 执行规则
     * @param content
     */
    CONTENT execute(CONTENT content) throws Exception;

}
