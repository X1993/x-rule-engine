package com.github.xengine.core;

import java.util.concurrent.Future;

/**
 * 节点执行器
 * @author X1993
 * @date 2023/2/10
 * @description
 */
public interface XNodeExecutor {

    /**
     * 根据规则的依赖顺序执行
     * @param startNode
     * @param ruleContent
     * @return
     */
    <CONTENT extends XRuleContent> Future<CONTENT> exe(XNode<CONTENT> startNode ,CONTENT ruleContent);

}
