package com.github.xengine.core;

/**
 * 执行逻辑为空，通常作为 起始/终止 节点
 * @author wangjj7
 * @date 2023/2/10
 * @description
 */
public class EmptyXRule<CONTENT extends XRuleContent<CONTENT>> implements XRule<CONTENT>{

    private final String name;

    public EmptyXRule(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public CONTENT execute(CONTENT content) {
        //不需要做任何事情
        return content;
    }

}
