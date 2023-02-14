package com.github.xengine.core;

import java.util.concurrent.Future;

/**
 * 规则执行器
 * @author wangjj7
 * @date 2023/2/10
 * @description
 */
public interface XRuleExecutor {

    /**
     * 规则异步执行
     * @param xRule
     * @param inputRuleContent
     * @param callback
     * @param <CONTENT>
     */
    <CONTENT extends XRuleContent<CONTENT>> Future<CONTENT> async(
            XRule<CONTENT> xRule ,CONTENT inputRuleContent ,Callback<CONTENT> callback);

    /**
     * 回调接口
     */
    interface Callback<CONTENT extends XRuleContent<CONTENT>> {

        /**
         * 规则执行结束之前调用
         * @param inputRuleContent 输入的上下文
         * @return false:规则不执行
         */
        boolean preExe(CONTENT inputRuleContent);

        /**
         * 规则执行结束之后调用
         * 如果{@link #preExe(XRuleContent)}==false，不执行
         * @param outputRuleContent 输出的上下文
         * @param e 没有异常：null
         */
        void postExe(Exception e ,CONTENT outputRuleContent);

    }

}
