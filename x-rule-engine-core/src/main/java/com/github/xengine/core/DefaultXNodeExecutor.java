package com.github.xengine.core;

import lombok.extern.slf4j.Slf4j;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 默认的规则节点执行器
 * @author X1993
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
    public <CONTENT extends XRuleContent> Future<CONTENT> exe(XNode<CONTENT> startNode, CONTENT ruleContent)
    {
        if (ruleContent == null){
            throw new IllegalArgumentException("任务上下文不能为空");
        }
        XNodeUtils.validationStartNode(startNode);
        CompletableFuture<CONTENT> future = new CompletableFuture<>();
        tryRunNode(startNode ,ruleContent,future);
        return future;
    }

    private <CONTENT extends XRuleContent> void tryRunNode(
            XNode<CONTENT> xNode ,CONTENT content ,CompletableFuture<CONTENT> future)
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
            //避免重复执行
            return;
        }

        String ruleName = xNode.getRule().name();
        log.debug("规则【{}】已就绪，等待执行" ,ruleName);
        
        Future<Void> ruleFuture = xRuleExecutor.exe(xNode.getRule(), content,
                new XRuleExecutor.Callback() {

                    @Override
                    public boolean preExe() {
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
                    public void postExe(Exception e)
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

                        if (xNode.complete()){
                            log.debug("规则【{}】执行成功，耗时{}毫秒", ruleName, durationMS);

                            if (xNode.isTerminationNode()) {
                                future.complete(content);
                                return;
                            }
                        }

                        Set<XNode<CONTENT>> postNodes = xNode.getPostNodes();

                        for (XNode postNode : postNodes) {
                            //执行可执行的后置节点
                            tryRunNode(postNode ,content ,future);
                        }
                    }
                });

        xNode.setRuleFuture(ruleFuture);
    }

}
