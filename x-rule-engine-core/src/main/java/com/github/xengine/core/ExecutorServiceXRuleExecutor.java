package com.github.xengine.core;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 基于线程池实现异步规则执行器
 * @author X1993
 * @date 2023/2/10
 * @description
 */
@Slf4j
public class ExecutorServiceXRuleExecutor implements XRuleExecutor {

    private final ExecutorService executorService;

    public ExecutorServiceXRuleExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public <CONTENT extends XRuleContent> Future exe(XRule<CONTENT> xRule, CONTENT content, Callback callback)
    {
        return executorService.submit(() -> execute(xRule ,content ,callback));
    }

    private <CONTENT extends XRuleContent> void execute(XRule<CONTENT> xRule, CONTENT content, Callback callback)
    {
        if (callback != null && !callback.preExe()){
            return;
        }
        Exception exception = null;
        try {
            xRule.execute(content);
        } catch (Exception e) {
            exception = e;
        } finally {
            if (callback != null) {
                callback.postExe(exception);
            }
        }
    }

}
