package com.github.xengine.core;

import lombok.extern.slf4j.Slf4j;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 默认的规则节点执行器
 * @author wangjj7
 * @date 2023/2/10
 * @description
 */
@Slf4j
public class DefaultXNodeExecutor implements XNodeExecutor {

    private final XRuleExecutor xRuleExecutor;

    public DefaultXNodeExecutor(XRuleExecutor xRuleExecutor) {
        this.xRuleExecutor = xRuleExecutor;
    }

    @Override
    public <CONTENT extends XRuleContent<CONTENT>> Future<CONTENT> execute(XNode<CONTENT> startNode, CONTENT content)
    {
        validation(startNode);
        startNode.mergePreOutput(content);
        CompletableFuture<CONTENT> future = new CompletableFuture<>();
        tryRunNode(startNode ,future);
        return future;
    }

    private <CONTENT extends XRuleContent<CONTENT>> void validation(XNode<CONTENT> startNode){
        if (!startNode.isStartNode()){
            throw new XNodeException("起始节点不能有前置节点");
        }
        List<XNode<CONTENT>> nodes = startNode.reachableNodes(false ,true);
        if (nodes.stream().filter(XNode::isStartNode).count() != 1){
            throw new XNodeException("起始节点有且只能有一个");
        }
        if (nodes.stream().filter(XNode::isTerminationNode).count() != 1){
            throw new XNodeException("终止节点有且只能有一个");
        }
        //终止节点只允许有一个
    }

    private <CONTENT extends XRuleContent<CONTENT>> void tryRunNode(XNode<CONTENT> xNode ,CompletableFuture<CONTENT> future)
    {
        int undonePreNodeCount = xNode.preNodeUndoneCount();
        if (undonePreNodeCount > 0) {
//            log.debug("规则【{}】还有{}个前置节点未完成,继续等待" ,xNode.getRule().name() ,undonePreNodeCount);
            return;
        }

        if (future.isCompletedExceptionally()){
            return;
        }

        if (!xNode.ready()){
            //确保每个节点只被提交一次
            return;
        }

        Set<XNode<CONTENT>> preNodes = xNode.getPreNodes();
        for (XNode<CONTENT> preNode : preNodes) {
            //将前置节点输出上下文合并到当前节点的输入上下文
            xNode.mergePreOutput(preNode.getOutputRuleContent());
        }

        String ruleName = xNode.getRule().name();
        log.debug("规则【{}】已就绪，等待执行" ,ruleName);

        final CONTENT inputRuleContent = xNode.getInputRuleContent();
        
        Future<CONTENT> ruleFuture = xRuleExecutor.async(xNode.getRule(), inputRuleContent.create(),
                new XRuleExecutor.Callback<CONTENT>() {

                    @Override
                    public boolean preExe(CONTENT inputRuleContent) {
                        if (future.isCompletedExceptionally()) {
                            if (xNode.cancel()) {
                                log.debug("任务异常，取消当前规则【{}】的执行", ruleName);
                            }
                            return false;
                        }
                        if (xNode.executing()) {
                            log.debug("规则【{}】开始执行", ruleName);
                        }
                        return true;
                    }

                    @Override
                    public void postExe(Exception e, CONTENT outputRuleContent)
                    {
                        long durationMS = xNode.getDuration(ChronoUnit.MILLIS);

                        if (e != null) {
                            if (e instanceof InterruptedException && future.isCompletedExceptionally()){
                                log.debug("规则【{}】被中断执行，耗时{}毫秒" ,ruleName ,durationMS);
                                xNode.intercept();
                                return;
                            }

                            log.error("规则【{}】执行异常，耗时{}毫秒", ruleName, durationMS ,e);
                            if (xNode.exception(e ,true)){
                                future.completeExceptionally(e);
                            }

                            return;
                        }

                        if (outputRuleContent == null) {
                            log.warn("规则输出的上下文不建议返回null，默认取输入上下文");
                            outputRuleContent = inputRuleContent;
                        }

                        if (xNode.complete(outputRuleContent)){
                            log.debug("规则【{}】执行成功，耗时{}毫秒", ruleName, durationMS);

                            if (xNode.isTerminationNode()) {
                                future.complete(xNode.getOutputRuleContent());
                                return;
                            }
                        }

                        Set<XNode<CONTENT>> postNodes = xNode.getPostNodes();

                        for (XNode postNode : postNodes) {
                            //执行可执行的后置节点
                            tryRunNode(postNode, future);
                        }
                    }
                });

        xNode.setRuleFuture(ruleFuture);
    }

}
