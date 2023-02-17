package com.github.xengine.core;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Future;

/**
 * 节点上下文，定义规则的依赖关系和执行状态
 * @author X1993
 * @date 2023/2/10
 * @description
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XNodeContent {

    /**
     * 规则执行的开始时间
     */
    LocalDateTime startTime;

    /**
     * 规则执行的结束时间
     */
    LocalDateTime endTime;

    /**
     * 规则状态
     */
    XRuleStatus ruleStatus = XRuleStatus.WAIT;

    /**
     * 规则执行异常
     */
    Exception exception;

    /**
     * 规则提交后的Future
     */
    Future ruleFuture;

    /**
     * 规则执行时间
     * @return
     */
    public long getDuration(ChronoUnit chronoUnit){
        return startTime.until(endTime == null ? LocalDateTime.now() : endTime ,chronoUnit);
    }

}
