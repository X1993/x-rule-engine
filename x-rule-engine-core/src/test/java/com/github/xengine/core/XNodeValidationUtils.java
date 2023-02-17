package com.github.xengine.core;

import com.github.xengine.core.mock.MockRuleContent;
import org.junit.Assert;

/**
 * @author X1993
 * @date 2023/2/14
 * @description
 */
public class XNodeValidationUtils {

    public static void exeSequence(XNode<? extends MockRuleContent> xNode){
        for (XNode<? extends MockRuleContent> postNode : xNode.getPostNodes()) {
            if (!postNode.getRuleStatus().isFinal()){
                continue;
            }
            Assert.assertFalse(xNode.getEndTime().isAfter(postNode.getStartTime()));
            exeSequence(postNode);
        }
    }

}
