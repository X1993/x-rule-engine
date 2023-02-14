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
 * @author wangjj7
 * @date 2023/2/10
 * @description
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XNodeContent<CONTENT extends XRuleContent<CONTENT>> implements Cloneable {

    /**
     * 规则执行开始时间
     */
    LocalDateTime startTime;

    /**
     * 规则执行结束时间
     */
    LocalDateTime endTime;

    /**
     * 规则状态
     */
    XRuleStatus ruleStatus;

    /**
     * 规则执行异常
     */
    Exception exception;

    /**
     * 输入的规则上下文
     */
    CONTENT inputRuleContent;

    /**
     * 输出的规则上下文
     */
    CONTENT outputRuleContent;

    /**
     * 规则提交后的Future
     */
    Future<CONTENT> ruleFuture;

    /**
     * 规则执行时间
     * @return
     */
    public long getDuration(ChronoUnit chronoUnit){
        return startTime.until(endTime == null ? LocalDateTime.now() : endTime ,chronoUnit);
    }

    @Override
    public XNodeContent<CONTENT> clone(){
        XNodeContent<CONTENT> clone = null;
        try {
            clone = (XNodeContent<CONTENT>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException();
        }
        return clone;
    }

}
