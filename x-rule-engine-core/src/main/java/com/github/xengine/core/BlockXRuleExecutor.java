package com.github.xengine.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 阻塞式同步规则执行器
 * @author wangjj7
 * @date 2023/2/14
 * @description
 */
public class BlockXRuleExecutor implements XRuleExecutor {

    @Override
    public <CONTENT extends XRuleContent<CONTENT>> Future async(XRule<CONTENT> xRule, CONTENT inputRuleContent, Callback<CONTENT> callback) {
        CompletableFuture<CONTENT> future = new CompletableFuture<>();

        if (callback != null && !callback.preExe(inputRuleContent)){
            future.cancel(false);
            return future;
        }

        CONTENT outputRuleContent = inputRuleContent;
        Exception exception = null;
        try {
            outputRuleContent = xRule.execute(inputRuleContent);
            future.complete(outputRuleContent == null ? inputRuleContent : outputRuleContent);
        } catch (Exception e) {
            exception = e;
            future.completeExceptionally(e);
        } finally {
            if (callback != null) {
                callback.postExe(exception, outputRuleContent);
            }
            return future;
        }
    }

}
