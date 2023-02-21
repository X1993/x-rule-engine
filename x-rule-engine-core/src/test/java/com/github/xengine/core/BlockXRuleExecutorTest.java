package com.github.xengine.core;

import com.github.xengine.core.mock.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.*;

/**
 * @author X1993
 * @date 2023/2/14
 * @description
 */
@Slf4j
public class BlockXRuleExecutorTest {

    @Test
    public void executeTest()
    {
        XNodeExecutor nodeExecutor = new DefaultXNodeExecutor(new BlockXRuleExecutor());

        XNode<MockRuleContent> startNode = new XNode<MockRuleContent>(new EmptyXRule<>("START"));
        XNode<MockRuleContent> endNode = new XNode<MockRuleContent>(new EmptyXRule<>("END"));

        XNode<MockRuleContent> node0_0 = new XNode(new MockRule(1 ,"0-0").setExecuteMS(20));
        XNode<MockRuleContent> node1_0 = new XNode(new MockRule(2 ,"1-0").setExecuteMS(100));
        XNode<MockRuleContent> node2_0 = new XNode(new MockRule(3 ,"2-0").setExecuteMS(50));

        XNode<MockRuleContent> node0_1 = new XNode(new MockRule(4 ,"0-1").setExecuteMS(70));

        startNode.addPostNode(node0_0 ,node1_0 ,node2_0);
        node0_0.addPostNode(node0_1);
        endNode.addPreNode(node0_1 ,node1_0, node2_0);

        //测试执行顺序及执行结果
        log.debug("---------------测试执行顺序及执行结果 start--------------");
        long startTimestamp = System.currentTimeMillis();

        Future<MockRuleContent> future = nodeExecutor.exe(startNode, new MockRuleContent().setFold(3));
        MockRuleContent resultRuleContent = null;
        try {
            resultRuleContent = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        long timeConsumingMS = System.currentTimeMillis() - startTimestamp;
        log.debug("耗时{}毫秒" ,timeConsumingMS);
        Assert.assertTrue(timeConsumingMS > 240);

        XNodeValidationUtils.validationSequence(startNode);

        Assert.assertTrue(30 == resultRuleContent.getSum().get());

        log.debug("---------------测试执行顺序及执行结果 end--------------");
    }
}