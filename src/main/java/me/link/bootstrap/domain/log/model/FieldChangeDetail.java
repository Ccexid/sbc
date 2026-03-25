package me.link.bootstrap.domain.log.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字段变更详情模型
 * 优化点：区分中英文名称、统一字符串存储以保证序列化兼容性、增加 Swagger 注解
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字段变更明细")
public class FieldChangeDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字段业务描述名称 (来自 @LogField 或 @Schema)
     * 示例："用户姓名"
     */
    @Schema(description = "字段业务描述", example = "用户状态")
    private String fieldLabel;

    /**
     * 字段技术名称
     * 示例："username"
     */
    @Schema(description = "字段属性名", example = "status")
    private String fieldName;

    /**
     * 变更前的原始值 (格式化后的字符串)
     * 理由：统一转为 String 存储，避免复杂对象在 JSON 序列化时产生格式不一致
     */
    @Schema(description = "旧值", example = "启用")
    private String beforeValue;

    /**
     * 变更后的新值 (格式化后的字符串)
     */
    @Schema(description = "新值", example = "禁用")
    private String afterValue;

    /**
     * 快速构建方法 (静态工厂)
     */
    public static FieldChangeDetail of(String label, String name, String before, String after) {
        return FieldChangeDetail.builder()
                .fieldLabel(label)
                .fieldName(name)
                .beforeValue(before)
                .afterValue(after)
                .build();
    }
}