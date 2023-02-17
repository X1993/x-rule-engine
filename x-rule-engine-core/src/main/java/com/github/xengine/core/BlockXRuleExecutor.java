package com.github.xengine.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 阻塞式同步规则执行器
 * @author X1993
 * @date 2023/2/14
 * @description
 */
public class BlockXRuleExecutor implements XRuleExecutor {

    @Override
    public <CONTENT extends XRuleContent> Future exe(XRule<CONTENT> xRule ,CONTENT content ,Callback callback)
    {
        CompletableFuture<CONTENT> future = new CompletableFuture<>();

        if (callback != null && !callback.preExe()){
            future.cancel(false);
            return future;
        }

        Exception exception = null;
        try {
            xRule.execute(content);
            future.complete(content);
        } catch (Exception e) {
            exception = e;
            future.completeExceptionally(e);
        } finally {
            if (callback != null) {
                callback.postExe(exception);
            }
            return future;
        }
    }

}
