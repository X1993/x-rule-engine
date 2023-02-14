package com.github.xengine.core;

/**
 * 规则上下文，保存规则的输入，输出和全局变量
 * 如果存在并发规则，需要确保上下文线程安全
 * @author wangjj7
 * @param <CHILD> 子类型
 * @date 2023/2/10
 * @description
 */
public interface XRuleContent<CHILD extends XRuleContent<CHILD>> {

    /**
     * 创建一个新的规则上下文
     * @return
     */
    CHILD create();

    /**
     * 聚合规则的上下文
     * @param ruleContent
     * @return
     */
    CHILD merge(CHILD ruleContent);

}
