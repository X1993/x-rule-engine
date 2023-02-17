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
    import com.github.xengine.demo.rule.*;
    import org.junit.Test;
    import java.util.concurrent.*;

    public class Demo {

        @Test
        public void run()
        {
            XNodeExecutor nodeExecutor = new DefaultXNodeExecutor(
                    new ExecutorServiceXRuleExecutor(new ThreadPoolExecutor(4 ,4 ,
                            60L ,TimeUnit.SECONDS ,new ArrayBlockingQueue<>(1000))));
    
            XNode<MyRuleContent> startNode = new XNode<>(new EmptyXRule<>("开始"));
            XNode<MyRuleContent> endNode = new XNode<>(new EmptyXRule<>("结束"));
    
            //初始化规则对象
            XNode<MyRuleContent> rule1 = new XNode(new Rule1().setExecuteMS(100));
            XNode<MyRuleContent> rule2 = new XNode(new Rule2().setExecuteMS(200));
            XNode<MyRuleContent> rule3 = new XNode(new Rule3().setExecuteMS(100));
            XNode<MyRuleContent> rule4 = new XNode(new Rule4().setExecuteMS(200));
            XNode<MyRuleContent> rule5 = new XNode(new Rule5().setExecuteMS(100));
    
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
[DEBUG] 20:33:53,252 [main] 规则【开始】已就绪，等待执行
[DEBUG] 20:33:53,275 [pool-1-thread-1] 规则【开始】开始执行
[DEBUG] 20:33:53,278 [pool-1-thread-1] 规则【开始】执行成功，耗时1毫秒
[DEBUG] 20:33:53,278 [pool-1-thread-1] 规则【Rule1()】已就绪，等待执行
[DEBUG] 20:33:53,279 [pool-1-thread-2] 规则【Rule1()】开始执行
[DEBUG] 20:33:53,387 [pool-1-thread-2] 规则【Rule1()】执行成功，耗时108毫秒
[DEBUG] 20:33:53,387 [pool-1-thread-2] 规则【Rule3()】已就绪，等待执行
[DEBUG] 20:33:53,387 [pool-1-thread-2] 规则【Rule2()】已就绪，等待执行
[DEBUG] 20:33:53,387 [pool-1-thread-3] 规则【Rule3()】开始执行
[DEBUG] 20:33:53,388 [pool-1-thread-4] 规则【Rule2()】开始执行
[DEBUG] 20:33:53,499 [pool-1-thread-3] 规则【Rule3()】执行成功，耗时112毫秒
[DEBUG] 20:33:53,499 [pool-1-thread-3] 规则【Rule4()】已就绪，等待执行
[DEBUG] 20:33:53,499 [pool-1-thread-3] 规则【Rule4()】开始执行
[DEBUG] 20:33:53,594 [pool-1-thread-4] 规则【Rule2()】执行成功，耗时206毫秒
[DEBUG] 20:33:53,703 [pool-1-thread-3] 规则【Rule4()】执行成功，耗时204毫秒
[DEBUG] 20:33:53,703 [pool-1-thread-3] 规则【Rule5()】已就绪，等待执行
[DEBUG] 20:33:53,704 [pool-1-thread-3] 规则【Rule5()】开始执行
[DEBUG] 20:33:53,813 [pool-1-thread-3] 规则【Rule5()】执行成功，耗时109毫秒
[DEBUG] 20:33:53,813 [pool-1-thread-3] 规则【结束】已就绪，等待执行
[DEBUG] 20:33:53,813 [pool-1-thread-3] 规则【结束】开始执行
[DEBUG] 20:33:53,813 [pool-1-thread-3] 规则【结束】执行成功，耗时0毫秒
```

[代码位置](x-rule-engine-core/src/test/java/com/github/xengine/demo)