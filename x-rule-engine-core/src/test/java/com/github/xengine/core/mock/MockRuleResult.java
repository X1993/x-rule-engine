package com.github.xengine.core.mock;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author X1993
 * @date 2023/2/14
 * @description
 */
@Data
@Accessors(chain = true)
public class MockRuleResult {

    /**
     * 规则名
     */
    String ruleName;

    /**
     * 结果
     */
    int result;

}
