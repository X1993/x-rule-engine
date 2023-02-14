package com.github.xengine.core.mock;

import com.github.xengine.core.XRuleContent;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟规则上下文
 * @author wangjj7
 * @date 2023/2/10
 * @description
 */
@Data
@Accessors(chain = true)
public class MockRuleContent implements XRuleContent<MockRuleContent> {

    private Map<String ,MockRuleResult> resultMap = new ConcurrentHashMap<>();

    private int sum;

    private int ruleResultVal;

    /**
     * true:所有规则上下文使用同一个
     * false:每个节点的规则上下文独立保存
     * 结合实际情况选择
     */
    private boolean global = false;

    @Override
    public MockRuleContent create() {
        if (global) {
            return this;
        }else {
            return deepClone();
        }
    }

    @Override
    public MockRuleContent merge(MockRuleContent ruleContent) {
        for (Map.Entry<String, MockRuleResult> entry : ruleContent.getResultMap().entrySet()) {
            resultMap.put(entry.getKey() ,entry.getValue());
        }
        return this;
    }

    public void addResult(MockRuleResult mockRuleResult){
        resultMap.put(mockRuleResult.getRuleName() ,mockRuleResult);
    }

    public List<MockRuleResult> getResults() {
        return new ArrayList<>(resultMap.values());
    }

    private MockRuleContent deepClone(){
        MockRuleContent clone = new MockRuleContent();
        clone.setResultMap(new ConcurrentHashMap<>(resultMap));
        return clone;
    }

}
