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
public class ExecutorServiceXRuleExecutorTest {

    @Test
    public void executeTest() {
        XNodeExecutor nodeExecutor = new DefaultXNodeExecutor(
                new ExecutorServiceXRuleExecutor(new ThreadPoolExecutor(
                        3 ,3 ,60L ,
                        TimeUnit.SECONDS ,new ArrayBlockingQueue<>(1000))));

        XNode<MockRuleContent> startNode = new XNode<>(new EmptyXRule<>("START"));
        XNode<MockRuleContent> endNode = new XNode<>(new EmptyXRule<>("END"));

        XNode<MockRuleContent> node0_0 = new XNode(new MockRule(1 ,"0-0").setExecuteMS(20));
        XNode<MockRuleContent> node1_0 = new XNode(new MockRule(2 ,"1-0").setExecuteMS(100));
        XNode<MockRuleContent> node2_0 = new XNode(new MockRule(3 ,"2-0").setExecuteMS(50));

        XNode<MockRuleContent> node0_1 = new XNode(new MockRule(4 ,"0-1").setExecuteMS(70));

        startNode.addPostNode(node0_0 ,node1_0 ,node2_0);
        node0_0.addPostNode(node0_1);
        endNode.addPreNode(node0_1 ,node1_0, node2_0);

        //测试执行顺序及执行结果
        log.debug("---------------测试执行顺序及执行结果 start--------------");
        Future<MockRuleContent> future = nodeExecutor.exe(startNode, new MockRuleContent().setFold(3));
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

        XNodeValidationUtils.validationSequence(startNode);

        Assert.assertTrue(3 * 10 == mockRuleContent.getSum().get());

        log.debug("---------------测试执行顺序及执行结果 end--------------");
    }

    @Test
    public void exceptionNodeTest() throws InterruptedException
    {
        XNodeExecutor nodeExecutor = new DefaultXNodeExecutor(
                new ExecutorServiceXRuleExecutor(new ThreadPoolExecutor(
                        4 ,4 ,60L ,
                        TimeUnit.SECONDS ,new ArrayBlockingQueue<>(1000))));

        XNode<MockRuleContent> startNode = new XNode<>(new EmptyXRule<>("START"));
        XNode<MockRuleContent> endNode = new XNode<>(new EmptyXRule<>("END"));

        XNode<MockRuleContent> node0_0 = new XNode(new MockRule(1 ,"0-0").setExecuteMS(20));
        XNode<MockRuleContent> node1_0 = new XNode(new MockRule(2 ,"1-0").setExecuteMS(50));
        XNode<MockRuleContent> node2_0 = new XNode(new MockRule(3 ,"2-0").setExecuteMS(100));

        XNode<MockRuleContent> node0_1 = new XNode(new MockRule(4 ,"0-1").setExecuteMS(70));

        startNode.addPostNode(node0_0 ,node1_0 ,node2_0);
        node0_0.addPostNode(node0_1);
        endNode.addPreNode(node0_1 ,node1_0, node2_0);

        log.debug("----------------测试节点异常 start---------------");
        XNode<MockRuleContent> exceptionNode = new XNode(new MockRule(1, "异常").setException(true).setExecuteMS(50));
        startNode.addPostNode(exceptionNode);
        exceptionNode.addPostNode(endNode);

        Future<MockRuleContent> future1 = nodeExecutor.exe(startNode, new MockRuleContent());
        boolean exception = false;
        try {
            future1.get();
        } catch (ExecutionException e){
            exception = true;
        }
        Assert.assertTrue(exception);

        Assert.assertTrue(node0_0.getRuleStatus() == XRuleStatus.COMPLETE);
        Assert.assertTrue(exceptionNode.getRuleStatus() == XRuleStatus.EXCEPTION);
        Assert.assertTrue(endNode.getRuleStatus() == XRuleStatus.WAIT);
        Assert.assertTrue(startNode.getRuleStatus() == XRuleStatus.COMPLETE);

        XNodeValidationUtils.validationSequence(startNode);

        Thread.sleep(500);

        log.debug("---------------测试节点异常 end---------------");
    }

    @Test
    public void executeTimeoutNodeTest() throws InterruptedException
    {
        XNodeExecutor nodeExecutor = new DefaultXNodeExecutor(
                new ExecutorServiceXRuleExecutor(new ThreadPoolExecutor(
                        4 ,4 ,60L ,
                        TimeUnit.SECONDS ,new ArrayBlockingQueue<>(1000))) ,
                new XEngineProperties().setDefaultRuleExeTimeoutMS(1000));

        XNode<MockRuleContent> startNode = new XNode<>(new EmptyXRule<>("START"));
        XNode<MockRuleContent> endNode = new XNode<>(new EmptyXRule<>("END"));

        XNode<MockRuleContent> node0_0 = new XNode(new MockRule(1 ,"0-0").setExecuteMS(20));
        XNode<MockRuleContent> node1_0 = new XNode(new MockRule(2 ,"1-0").setExecuteMS(50));
        XNode<MockRuleContent> node2_0 = new XNode(new MockRule(3 ,"2-0").setExecuteMS(100));

        XNode<MockRuleContent> node0_1 = new XNode(new MockRule(4 ,"0-1").setExecuteMS(70));

        startNode.addPostNode(node0_0 ,node1_0 ,node2_0);
        node0_0.addPostNode(node0_1);
        endNode.addPreNode(node0_1 ,node1_0, node2_0);

        log.debug("----------------测试节点执行超时 start---------------");
        XNode<MockRuleContent> timeoutNode = new XNode(new MockRule(1, "超时").setExecuteMS(2000));
        startNode.addPostNode(timeoutNode);
        timeoutNode.addPostNode(endNode);

        Future<MockRuleContent> future1 = nodeExecutor.exe(startNode, new MockRuleContent());
        boolean exception = false;
        try {
            future1.get();
        } catch (ExecutionException e){
            exception = true;
            Assert.assertTrue(e.getCause() instanceof XRuleTimeoutException);
        }
        Assert.assertTrue(exception);

        Assert.assertTrue(startNode.getRuleStatus() == XRuleStatus.COMPLETE);
        Assert.assertTrue(node0_0.getRuleStatus() == XRuleStatus.COMPLETE);
        Assert.assertTrue(timeoutNode.getRuleStatus() == XRuleStatus.EXCEPTION);
        Assert.assertTrue(endNode.getRuleStatus() == XRuleStatus.WAIT);

        XNodeValidationUtils.validationSequence(startNode);

        Thread.sleep(500);

        log.debug("---------------测试节点执行异常 end---------------");
    }

    @Test
    public void readyTimeoutWarnTest() throws InterruptedException, ExecutionException {
        XNodeExecutor nodeExecutor = new DefaultXNodeExecutor(
                new ExecutorServiceXRuleExecutor(new ThreadPoolExecutor(
                        1 ,1 ,60L ,
                        TimeUnit.SECONDS ,new ArrayBlockingQueue<>(1000))) ,
                new XEngineProperties().setReadyTimeoutWarnMS(200));

        XNode<MockRuleContent> node0_0 = new XNode(new MockRule(1 ,"0-0").setExecuteMS(500));
        XNode<MockRuleContent> node1_0 = new XNode(new MockRule(1 ,"1-0").setExecuteMS(500));
        XNode<MockRuleContent> node2_0 = new XNode(new MockRule(1 ,"2-0").setExecuteMS(500));

        XNode<MockRuleContent> startNode = XNodeUtils.buildParallel(node0_0, node1_0, node2_0);

        log.debug("----------------测试节点就绪超时 start---------------");
        Future<MockRuleContent> future1 = nodeExecutor.exe(startNode, new MockRuleContent());
        future1.get();
        log.debug("---------------测试节点就绪异常 end---------------");
    }

}