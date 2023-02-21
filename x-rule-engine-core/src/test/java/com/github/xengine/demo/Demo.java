package com.github.xengine.demo;

import com.github.xengine.core.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.Test;
import java.util.concurrent.*;

/**
 * @author X1993
 * @date 2023/2/17
 * @description
 */
public class Demo {

    //自定义规则上下文
    @Data
    class MyRuleContent implements XRuleContent{}

    //自定义规则
    @Data
    @Accessors(chain = true)
    public class MockRule implements XRule<MyRuleContent> {

        //规则执行时间
        private long executeMS;

        //规则名
        private String name;

        @Override
        public String name() {
            return name;
        }

        @Override
        public void execute(MyRuleContent myRuleContent) throws Exception {
            if (executeMS > 0){
                //模拟规则执行
                Thread.sleep(executeMS);
            }
        }
    }

    @Test
    public void run()
    {
        XNodeExecutor nodeExecutor = new DefaultXNodeExecutor(
                new ExecutorServiceXRuleExecutor(new ThreadPoolExecutor(4 ,4 ,
                        60L ,TimeUnit.SECONDS ,new ArrayBlockingQueue<>(1000))));

        XNode<MyRuleContent> startNode = new XNode<>(new EmptyXRule<>("START"));
        XNode<MyRuleContent> endNode = new XNode<>(new EmptyXRule<>("END"));
        
        //初始化规则对象
        XNode<MyRuleContent> rule1 = new XNode(new MockRule().setName("1").setExecuteMS(100));
        XNode<MyRuleContent> rule2 = new XNode(new MockRule().setName("2").setExecuteMS(200));
        XNode<MyRuleContent> rule3 = new XNode(new MockRule().setName("3").setExecuteMS(100));
        XNode<MyRuleContent> rule4 = new XNode(new MockRule().setName("4").setExecuteMS(200));
        XNode<MyRuleContent> rule5 = new XNode(new MockRule().setName("5").setExecuteMS(100));

        //编辑规则相对执行顺序
        startNode.addPostNode(rule1);
        rule1.addPostNode(rule2 ,rule3);
        rule3.addPostNode(rule4);
        rule4.addPostNode(rule5);
        rule2.addPostNode(rule5);
        rule5.addPostNode(endNode);

        Future<MyRuleContent> future = nodeExecutor.exe(startNode, new MyRuleContent());

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
