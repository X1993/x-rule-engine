package com.github.xengine.core;

import com.github.xengine.core.mock.MockRule;
import com.github.xengine.core.mock.MockRuleContent;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.*;

/**
 * @author X1993
 * @date 2023/2/17
 * @description
 */
@Slf4j
public class XNodeUtilsTest {

    @Test
    public void testValidationCycle() {
        XNode<MockRuleContent> startNode = new XNode<MockRuleContent>(new EmptyXRule<>("START"));
        XNode<MockRuleContent> endNode = new XNode<MockRuleContent>(new EmptyXRule<>("END"));

        XNode<MockRuleContent> node0_0 = new XNode(new EmptyXRule("0-0"));
        XNode<MockRuleContent> node1_0 = new XNode(new EmptyXRule("1-0"));
        XNode<MockRuleContent> node2_0 = new XNode(new EmptyXRule("2-0"));

        XNode<MockRuleContent> node0_1 = new XNode(new EmptyXRule("0-1"));
        XNode<MockRuleContent> node0_2 = new XNode(new EmptyXRule("0-2"));

        startNode.addPostNode(node0_0 ,node1_0 ,node2_0);
        node0_0.addPostNode(node0_1);
        node0_1.addPostNode(node0_2);

        //【0-0】-【0-1】-【0-2】构成了环
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

    @Test
    public void buildParallelTest()
    {
        XNodeExecutor nodeExecutor = new DefaultXNodeExecutor(
                new ExecutorServiceXRuleExecutor(new ThreadPoolExecutor(
                        4 ,4 ,60L ,
                        TimeUnit.SECONDS ,new ArrayBlockingQueue<>(1000))));

        XNode<MockRuleContent> node0_0 = new XNode(new MockRule(1 ,"0-0").setExecuteMS(200));
        XNode<MockRuleContent> node1_0 = new XNode(new MockRule(2 ,"1-0").setExecuteMS(300));
        XNode<MockRuleContent> node2_0 = new XNode(new MockRule(3 ,"2-0").setExecuteMS(500));

        XNode<MockRuleContent> startNode = XNodeUtils.buildParallel(node0_0, node1_0, node2_0);
        Future<MockRuleContent> future = nodeExecutor.exe(startNode, new MockRuleContent().setFold(3));
        long startTimestamp = System.currentTimeMillis();

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        long timeConsumingMS = System.currentTimeMillis() - startTimestamp;
        log.debug("耗时{}毫秒" ,timeConsumingMS);
        Assert.assertTrue(timeConsumingMS < 650);

        XNodeValidationUtils.validationSequence(startNode);
    }

    @Test
    public void buildSerialTest()
    {
        XNodeExecutor nodeExecutor = new DefaultXNodeExecutor(
                new ExecutorServiceXRuleExecutor(new ThreadPoolExecutor(
                        4 ,4 ,60L ,
                        TimeUnit.SECONDS ,new ArrayBlockingQueue<>(1000))));

        XNode<MockRuleContent> node0_0 = new XNode(new MockRule(1 ,"0-0").setExecuteMS(200));
        XNode<MockRuleContent> node1_0 = new XNode(new MockRule(2 ,"1-0").setExecuteMS(300));
        XNode<MockRuleContent> node2_0 = new XNode(new MockRule(3 ,"2-0").setExecuteMS(500));

        XNode<MockRuleContent> startNode = XNodeUtils.buildSerial(node0_0, node1_0, node2_0);
        Future<MockRuleContent> future = nodeExecutor.exe(startNode, new MockRuleContent().setFold(3));
        long startTimestamp = System.currentTimeMillis();

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        long timeConsumingMS = System.currentTimeMillis() - startTimestamp;
        log.debug("耗时{}毫秒" ,timeConsumingMS);
        Assert.assertTrue(timeConsumingMS > 1000);

        XNodeValidationUtils.validationSequence(startNode);
    }

}