package me.link.bootstrap.shared.util.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段变更详情模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FieldChange {
    /**
     * 字段名称或描述（优先取自 @LogField 注解，否则为字段名）
     */
    private String fieldName;

    /**
     * 变更前的原始值
     */
    private Object oldValue;

    /**
     * 变更后的新值
     */
    private Object newValue;
}
