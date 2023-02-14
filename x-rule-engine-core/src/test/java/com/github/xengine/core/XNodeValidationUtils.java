package com.github.xengine.core;

import com.github.xengine.core.mock.MockRuleContent;
import org.junit.Assert;

/**
 * @author wangjj7
 * @date 2023/2/14
 * @description
 */
public class XNodeValidationUtils {

    public static void exeSequence(XNode<? extends MockRuleContent> xNode){
        for (XNode<? extends MockRuleContent> postNode : xNode.getPostNodes()) {
            if (!postNode.getRuleStatus().isFinal()){
                continue;
            }
            Assert.assertFalse(xNode.getNodeContent().getEndTime().isAfter(postNode.getNodeContent().getStartTime()));
            exeSequence(postNode);
        }
    }

}
