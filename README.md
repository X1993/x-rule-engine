### 简介:
一款支持根据配置的规则相对先后顺序并发执行的规则引擎

### 目录结构
```bash
|-- x-rule-engine-core                       ➜ 核心模块
```

### 效果演示
期望实现的效果
<p align="center">
  <a>
   <img alt="规则执行流程" src="x-rule-engine-core/演示规则流%20.jpg">
  </a>
</p>

* [x] 每个节点所有前置节点都完成才能开始执行
* [x] 每个节点的多个后置节点支持并发执行

演示代码
```java
    import com.github.xengine.core.*;
    import lombok.Data;
    import lombok.experimental.Accessors;
    import org.junit.Test;
    import java.util.concurrent.*;
    
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
    
            XNode<MyRuleContent> startNode = new XNode<>(new EmptyXRule<>("开始"));
            XNode<MyRuleContent> endNode = new XNode<>(new EmptyXRule<>("结束"));
    
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
```

日志
```text
[DEBUG] 16:09:16,878 [main] 规则【START】已就绪，等待执行
[DEBUG] 16:09:16,889 [pool-1-thread-1] 规则【START】开始执行
[DEBUG] 16:09:16,891 [pool-1-thread-1] 规则【START】执行成功，耗时1毫秒
[DEBUG] 16:09:16,891 [pool-1-thread-1] 规则【1】已就绪，等待执行
[DEBUG] 16:09:16,892 [pool-1-thread-2] 规则【1】开始执行
[DEBUG] 16:09:16,992 [pool-1-thread-2] 规则【1】执行成功，耗时100毫秒
[DEBUG] 16:09:16,992 [pool-1-thread-2] 规则【2】已就绪，等待执行
[DEBUG] 16:09:16,992 [pool-1-thread-2] 规则【3】已就绪，等待执行
[DEBUG] 16:09:16,993 [pool-1-thread-3] 规则【2】开始执行
[DEBUG] 16:09:16,993 [pool-1-thread-4] 规则【3】开始执行
[DEBUG] 16:09:17,103 [pool-1-thread-4] 规则【3】执行成功，耗时110毫秒
[DEBUG] 16:09:17,103 [pool-1-thread-4] 规则【4】已就绪，等待执行
[DEBUG] 16:09:17,103 [pool-1-thread-4] 规则【4】开始执行
[DEBUG] 16:09:17,194 [pool-1-thread-3] 规则【2】执行成功，耗时201毫秒
[DEBUG] 16:09:17,304 [pool-1-thread-4] 规则【4】执行成功，耗时201毫秒
[DEBUG] 16:09:17,304 [pool-1-thread-4] 规则【5】已就绪，等待执行
[DEBUG] 16:09:17,304 [pool-1-thread-4] 规则【5】开始执行
[DEBUG] 16:09:17,412 [pool-1-thread-4] 规则【5】执行成功，耗时108毫秒
[DEBUG] 16:09:17,412 [pool-1-thread-4] 规则【END】已就绪，等待执行
[DEBUG] 16:09:17,413 [pool-1-thread-4] 规则【END】开始执行
[DEBUG] 16:09:17,413 [pool-1-thread-4] 规则【END】执行成功，耗时0毫秒
```

[代码位置](x-rule-engine-core/src/test/java/com/github/xengine/demo)