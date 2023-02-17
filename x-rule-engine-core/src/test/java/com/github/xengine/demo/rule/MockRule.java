package com.github.xengine.demo.rule;

import com.github.xengine.core.XRule;
import com.github.xengine.demo.MyRuleContent;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 模拟规则
 * @author X1993
 * @date 2023/2/10
 * @description
 */
@Data
@Accessors(chain = true)
public abstract class MockRule implements XRule<MyRuleContent> {

    /**
     * 规则执行时间
     */
    private long executeMS;

    @Override
    public void execute(MyRuleContent myRuleContent) throws Exception {
        if (executeMS > 0){
            //模拟规则执行
            Thread.sleep(executeMS);
        }
    }

}
