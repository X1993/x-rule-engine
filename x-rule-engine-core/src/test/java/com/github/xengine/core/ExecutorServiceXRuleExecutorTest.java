package com.github.xengine.core;

import com.github.xengine.core.mock.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * @author wangjj7
 * @date 2023/2/14
 * @description
 */
@Slf4j
public class ExecutorServiceXRuleExecutorTest {
    
    @Test
    public void executeTest(){
        executeTest(new MockRuleContent().setGlobal(true));
        executeTest(new MockRuleContent().setGlobal(false));
    }

    private void executeTest(MockRuleContent ruleContent) {
        XNodeExecutor nodeExecutor = new DefaultXNodeExecutor(
                new ExecutorServiceXRuleExecutor(new ThreadPoolExecutor(
                        3 , 3 ,60L ,
                        TimeUnit.SECONDS ,new ArrayBlockingQueue<>(1000))));

        XNode<MockRuleContent> startNode = new XNode<>(new EmptyXRule<MockRuleContent>("启始节点"));
        XNode<MockRuleContent> endNode = new XNode<>(new EmptyXRule<MockRuleContent>("结束节点"));

        int node0_0Num = 1;
        int node1_0Num = 2;
        int node2_0Num = 3;
        int node0_1Num = 4;

        XNode<MockRuleContent> node0_0 = new XNode(new MockRule(node0_0Num ,"节点0-0").setExecuteMS(20));
        XNode<MockRuleContent> node1_0 = new XNode(new MockRule(node1_0Num ,"节点1-0").setExecuteMS(100));
        XNode<MockRuleContent> node2_0 = new XNode(new MockRule(node2_0Num ,"节点2-0").setExecuteMS(50));

        XNode<MockRuleContent> node0_1 = new XNode(new MockRule(node0_1Num ,"节点0-1").setExecuteMS(70));

        startNode.addPostNode(node0_0 ,node1_0 ,node2_0);
        node0_0.addPostNode(node0_1);
        endNode.addPreNode(node0_1 ,node1_0, node2_0);

        //测试执行顺序及执行结果
        log.debug("---------------测试执行顺序及执行结果 start--------------");
        Future<MockRuleContent> future = nodeExecutor.execute(startNode, ruleContent);
        MockRuleContent mockRuleContent = null;

        long startTimestamp = System.currentTimeMillis();
        try {
            mockRuleContent = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        long timeConsumingMS = System.currentTimeMillis() - startTimestamp;
        log.debug("耗时{}毫秒" ,timeConsumingMS);
        Assert.assertTrue(timeConsumingMS < 240);

        XNodeValidationUtils.exeSequence(startNode);

        List<MockRuleResult> results = mockRuleContent.getResults();
        Assert.assertTrue(IntStream.of(node0_0Num ,node1_0Num ,node2_0Num ,node0_1Num).sum()
                == results.stream().mapToInt(MockRuleResult::getResult).sum());

        log.debug("---------------测试执行顺序及执行结果 end--------------");
    }

    @Test
    public void exceptionNodeTest() throws InterruptedException {
        exceptionNodeTest(new MockRuleContent().setGlobal(true));
        exceptionNodeTest(new MockRuleContent().setGlobal(false));
    }

    private void exceptionNodeTest(MockRuleContent ruleContent) throws InterruptedException
    {
        XNodeExecutor nodeExecutor = new DefaultXNodeExecutor(
                new ExecutorServiceXRuleExecutor(new ThreadPoolExecutor(
                        4 , 4 ,60L ,
                        TimeUnit.SECONDS ,new ArrayBlockingQueue<>(1000))));

        XNode<MockRuleContent> startNode = new XNode<>(new EmptyXRule<MockRuleContent>("启始节点"));
        XNode<MockRuleContent> endNode = new XNode<>(new EmptyXRule<MockRuleContent>("结束节点"));

        int node0_0Num = 1;
        int node1_0Num = 2;
        int node2_0Num = 3;
        int node0_1Num = 4;

        XNode<MockRuleContent> node0_0 = new XNode(new MockRule(node0_0Num ,"节点0-0").setExecuteMS(20));
        XNode<MockRuleContent> node1_0 = new XNode(new MockRule(node1_0Num ,"节点1-0").setExecuteMS(50));
        XNode<MockRuleContent> node2_0 = new XNode(new MockRule(node2_0Num ,"节点2-0").setExecuteMS(100));

        XNode<MockRuleContent> node0_1 = new XNode(new MockRule(node0_1Num ,"节点0-1").setExecuteMS(70));

        startNode.addPostNode(node0_0 ,node1_0 ,node2_0);
        node0_0.addPostNode(node0_1);
        endNode.addPreNode(node0_1 ,node1_0, node2_0);

        log.debug("----------------测试节点异常 start---------------");
        XNode<MockRuleContent> exceptionNode = new XNode(new MockRule(node0_0Num, "异常节点").setException(true).setExecuteMS(50));
        startNode.addPostNode(exceptionNode);
        exceptionNode.addPostNode(endNode);

        Future<MockRuleContent> future1 = nodeExecutor.execute(startNode, ruleContent);
        boolean exception = false;
        try {
            future1.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e){
            exception = true;
        }
        Assert.assertTrue(exception);

        Assert.assertTrue(node0_0.getNodeContent().getRuleStatus() == XRuleStatus.COMPLETE);
        Assert.assertTrue(exceptionNode.getNodeContent().getRuleStatus() == XRuleStatus.EXCEPTION);
        Assert.assertTrue(endNode.getNodeContent().getRuleStatus() == XRuleStatus.WAIT);
        Assert.assertTrue(startNode.getNodeContent().getRuleStatus() == XRuleStatus.COMPLETE);

        XNodeValidationUtils.exeSequence(startNode);

        Thread.sleep(500);

        log.debug("---------------测试节点异常 end---------------");
    }

}