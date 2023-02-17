package com.github.xengine.core;

import com.github.xengine.core.mock.MockRuleContent;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author wangjj7
 * @date 2023/2/17
 * @description
 */
@Slf4j
public class XNodeUtilsTest {

    @Test
    public void testValidationCycle() {
        XNode<MockRuleContent> startNode = new XNode<MockRuleContent>(new EmptyXRule<>("启始节点"));
        XNode<MockRuleContent> endNode = new XNode<MockRuleContent>(new EmptyXRule<>("结束节点"));

        XNode<MockRuleContent> node0_0 = new XNode(new EmptyXRule("节点0-0"));
        XNode<MockRuleContent> node1_0 = new XNode(new EmptyXRule("节点1-0"));
        XNode<MockRuleContent> node2_0 = new XNode(new EmptyXRule("节点2-0"));

        XNode<MockRuleContent> node0_1 = new XNode(new EmptyXRule("节点0-1"));
        XNode<MockRuleContent> node0_2 = new XNode(new EmptyXRule("节点0-2"));

        startNode.addPostNode(node0_0 ,node1_0 ,node2_0);
        node0_0.addPostNode(node0_1);
        node0_1.addPostNode(node0_2);

        //【节点0-0】-【节点0-1】-【节点0-2】构成了环
        node0_2.addPostNode(node0_0);

        endNode.addPreNode(node0_1 ,node1_0, node2_0);

        boolean exception = false;
        try {
            XNodeUtils.validationCycle(startNode);
        }catch (XNodeException e){
            log.error(e.toString());
            exception = true;
        }
        Assert.assertTrue(exception);
    }
}