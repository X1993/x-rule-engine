package com.github.xengine.core;

import java.util.concurrent.Future;

/**
 * 规则执行器
 * @author X1993
 * @date 2023/2/10
 * @description
 */
public interface XRuleExecutor {

    /**
     * 规则异步执行
     * @param xRule 规则
     * @param content 规则上下文
     * @param callback 回调接口
     * @param <CONTENT>
     */
    <CONTENT extends XRuleContent> Future exe(XRule<CONTENT> xRule ,CONTENT content ,Callback callback);

    /**
     * 回调接口
     */
    interface Callback {

        /**
         * 规则执行前调用
         * @return false:规则不执行
         */
        boolean preExe();

        /**
         * 规则执行后调用
         * @param e 没有异常：null
         */
        void postExe(Exception e);

    }

}
