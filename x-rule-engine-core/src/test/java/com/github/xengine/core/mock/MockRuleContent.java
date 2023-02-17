package com.github.xengine.core.mock;

import com.github.xengine.core.XRuleContent;
import lombok.Data;
import lombok.experimental.Accessors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模拟规则上下文
 * @author X1993
 * @date 2023/2/10
 * @description
 */
@Data
@Accessors(chain = true)
public class MockRuleContent implements XRuleContent {

    /*-----------入参----------*/
    //倍数
    private int fold = 1;

    /*-----------输出----------*/
    private final AtomicInteger sum = new AtomicInteger();

    public void add(int num){
        sum.addAndGet(num);
    }

}
