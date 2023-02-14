package com.github.xengine.core.mock;

import com.github.xengine.core.XRule;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 模拟规则
 * @author wangjj7
 * @date 2023/2/10
 * @description
 */
@Data
@Accessors(chain = true)
public class MockRule implements XRule<MockRuleContent> {

    /**
     * 规则的执行结果
     */
    private final int ruleResult;

    /**
     * 规则名称
     */
    private final String name;

    /**
     * 是否抛异常
     */
    private boolean exception;

    /**
     * 规则执行时间
     */
    private long executeMS;

    public MockRule(int ruleResult, String name) {
        this.ruleResult = ruleResult;
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public MockRuleContent execute(MockRuleContent content) throws Exception {
        if (executeMS > 0){
            Thread.sleep(executeMS);
        }
        if (exception){
            throw new Exception("模拟异常");
        }
        content.addResult(new MockRuleResult().setRuleName(name).setResult(ruleResult));
        return content;
    }

}
