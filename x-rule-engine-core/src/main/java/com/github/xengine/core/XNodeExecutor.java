package com.github.xengine.core;

import java.util.concurrent.Future;

/**
 * 节点执行器
 * @author wangjj7
 * @date 2023/2/10
 * @description
 */
public interface XNodeExecutor {

    /**
     * 执行
     * @param startNode
     * @param content
     * @return
     */
    <CONTENT extends XRuleContent<CONTENT>> Future<CONTENT> execute(XNode<CONTENT> startNode ,CONTENT content);

    /**
     * 执行
     * @param startNode
     * @return
     */
    default <CONTENT extends XRuleContent<CONTENT>> Future<CONTENT> execute(XNode<CONTENT> startNode){
        return execute(startNode ,startNode.getInputRuleContent());
    }

}
