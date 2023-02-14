package com.github.xengine.core;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 基于线程池实现异步规则执行器
 * @author wangjj7
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
    public <CONTENT extends XRuleContent<CONTENT>> Future<CONTENT> async(
            XRule<CONTENT> xRule, CONTENT inputRuleContent, Callback<CONTENT> callback)
    {
        return executorService.submit(() -> execute(xRule , inputRuleContent,callback));
    }

    private <CONTENT extends XRuleContent<CONTENT>> CONTENT execute(
            XRule<CONTENT> xRule, CONTENT ruleContent ,Callback<CONTENT> callback)
    {
        if (callback != null && !callback.preExe(ruleContent)){
            return ruleContent;
        }
        CONTENT outputRuleContent = ruleContent;
        Exception exception = null;
        try {
            outputRuleContent = xRule.execute(ruleContent);
        } catch (Exception e) {
            exception = e;
        } finally {
            if (callback != null) {
                callback.postExe(exception, outputRuleContent);
            }
            return outputRuleContent;
        }
    }

}
