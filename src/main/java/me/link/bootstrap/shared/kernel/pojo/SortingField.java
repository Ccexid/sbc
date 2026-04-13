package me.link.bootstrap.shared.kernel.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排序字段实体类
 * <p>
 * 用于封装排序相关的字段信息，包括字段标识和排序方向。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "排序对象")
public class SortingField {

    /**
     * 字段标识 (Field)
     * <p>
     * 用于指定需要排序的字段名称或唯一标识符。
     * </p>
     */
    @Schema(description = "字段标识")
    private String field;

    /**
     * 是否升序
     * <p>
     * true 表示升序排列 (ASC)，false 表示降序排列 (DESC)。
     * </p>
     */
    @Schema(description = "是否升序")
    private boolean asc;
}
