package com.github.xengine.core;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author X1993
 * @date 2023/2/24
 * @description
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class XEngineProperties {

    /**
     * 规则处于就绪状态等待超时告警的阈值
     */
    long readyTimeoutWarnMS = 60L * 1000;

    /**
     * 默认的规则超时毫秒数
     */
    long defaultRuleExeTimeoutMS = 60L * 1000;

}
