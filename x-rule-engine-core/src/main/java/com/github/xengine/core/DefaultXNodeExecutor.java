package com.github.xengine.core;

import lombok.extern.slf4j.Slf4j;
import java.text.MessageFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 规则节点调度执行器
 * @author X1993
 * @date 2023/2/10
 * @description
 */
@Slf4j
public class DefaultXNodeExecutor implements XNodeExecutor {

    private static final ScheduledExecutorService TIMEOUT_SCHEDULER = Executors
            .newScheduledThreadPool(1 ,new DefaultThreadFactory());

    private final XRuleExecutor xRuleExecutor;

    private final XEngineProperties xEngineProperties;

    public DefaultXNodeExecutor(XRuleExecutor xRuleExecutor, XEngineProperties xEngineProperties) {
        this.xRuleExecutor = xRuleExecutor;
        this.xEngineProperties = xEngineProperties;
    }

    public DefaultXNodeExecutor(XRuleExecutor xRuleExecutor) {
        this(xRuleExecutor ,new XEngineProperties());
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
            XNode<CONTENT> xNode ,CONTENT content ,CompletableFuture<CONTENT> taskFuture)
    {
        int undonePreNodeCount = xNode.preNodeUndoneCount();
        if (undonePreNodeCount > 0) {
//            log.debug("规则【{}】还有{}个前置节点未完成,继续等待" ,xNode.getRule().name() ,undonePreNodeCount);
            return;
        }

        if (taskFuture.isCompletedExceptionally()){
            return;
        }

        if (!xNode.ready()){//确保每个节点只有一个线程可以执行后续操作
            return;
        }

        String ruleName = xNode.getRule().name();
        log.debug("规则【{}】已就绪，提交规则执行器" ,ruleName);
        
        Future<Void> ruleFuture = xRuleExecutor.exe(xNode.getRule(), content,
                new XRuleExecutor.Callback() {

                    @Override
                    public boolean preExe() {
                        if (taskFuture.isCompletedExceptionally()) {
                            if (xNode.cancel()) {
                                log.debug("任务异常，取消当前规则【{}】的执行", ruleName);
                            }
                            return false;
                        }
                        if (xNode.executing()) {
                            log.debug("规则【{}】开始执行", ruleName);
                        }
                        long readyDurationMS = xNode.getReadyDuration(ChronoUnit.MILLIS);
                        if (readyDurationMS > xEngineProperties.getReadyTimeoutWarnMS()){
                            log.warn("规则【{}】就绪时间长达{}毫秒" ,ruleName ,readyDurationMS);
                        }
                        registerTimeoutCallback(xNode ,taskFuture);
                        return true;
                    }

                    @Override
                    public void postExe(Exception e)
                    {
                        long executeDurationMS = xNode.getExecuteDuration(ChronoUnit.MILLIS);

                        if (e != null) {
                            if (e instanceof InterruptedException && taskFuture.isCompletedExceptionally()){
                                log.debug("规则【{}】被中断执行，耗时{}毫秒" ,ruleName ,executeDurationMS);
                                xNode.intercept();
                                return;
                            }

                            log.error("规则【{}】执行异常，耗时{}毫秒", ruleName, executeDurationMS ,e);
                            if (xNode.exception(e ,true)){
                                taskFuture.completeExceptionally(e);
                            }
                            return;
                        }

                        if (xNode.complete()){
                            log.debug("规则【{}】执行成功，耗时{}毫秒", ruleName, executeDurationMS);

                            if (xNode.isTerminationNode()) {
                                taskFuture.complete(content);
                                return;
                            }
                        }

                        Set<XNode<CONTENT>> postNodes = xNode.getPostNodes();

                        for (XNode postNode : postNodes) {
                            //执行可执行的后置节点
                            tryRunNode(postNode ,content ,taskFuture);
                        }
                    }
                });

        xNode.setRuleFuture(ruleFuture);
    }

    //注册超时回调
    private void registerTimeoutCallback(XNode xNode ,CompletableFuture taskFuture)
    {
        long ruleTimeoutMS = xNode.getRule().timeoutMS() > 0 ?
                xNode.getRule().timeoutMS() : xEngineProperties.getDefaultRuleExeTimeoutMS();

        TIMEOUT_SCHEDULER.schedule(() -> {
            if (xNode.getRuleStatus() == XRuleStatus.EXECUTING){
                long executeDurationMS = xNode.getExecuteDuration(ChronoUnit.MILLIS);
                String ruleName = xNode.getRule().name();
                log.error("规则【{}】执行{}毫秒未结束，超时" ,ruleName ,executeDurationMS);
                //超时回调
                XRuleTimeoutException e = new XRuleTimeoutException(MessageFormat.format(
                        "规则【{0}】执行{1}毫秒未结束，超时" ,ruleName ,executeDurationMS));
                taskFuture.completeExceptionally(e);
                xNode.exception(e ,true);
            }
        } ,ruleTimeoutMS ,TimeUnit.MILLISECONDS);
    }

    static class DefaultThreadFactory implements ThreadFactory
    {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "xRuleTimeout-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

}
