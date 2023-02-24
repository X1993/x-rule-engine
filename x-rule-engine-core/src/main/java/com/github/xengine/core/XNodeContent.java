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
     * 节点创建时间
     */
    LocalDateTime createTime = LocalDateTime.now();

    /**
     * 就绪时间
     * @see XRuleStatus#READY
     */
    LocalDateTime readyTime;

    /**
     * 规则执行时间
     * @see XRuleStatus#EXECUTING
     */
    LocalDateTime executeTime;

    /**
     * 规则执行结束时间
     * @see XRuleStatus#isFinal()
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
    public long getExeDuration(ChronoUnit chronoUnit){
        if (executeTime == null){
            return 0;
        }
        return executeTime.until(getNowIfNull(endTime) ,chronoUnit);
    }

    /**
     * 规则就绪时间
     * @return
     */
    public long getReadyDuration(ChronoUnit chronoUnit){
        if (readyTime == null){
            return 0;
        }
        return readyTime.until(getNowIfNull(executeTime) ,chronoUnit);
    }

    private LocalDateTime getNowIfNull(LocalDateTime localDateTime){
        return localDateTime == null ? LocalDateTime.now() : localDateTime;
    }

}
