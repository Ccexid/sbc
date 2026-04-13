package me.link.bootstrap.shared.kernel.pojo;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 可排序的分页参数
 * <p>
 * 继承自 {@link PageParam}，额外支持通过 {@code sort} 字段指定排序规则。
 * 排序格式示例：{@code -field1,field2} 表示按 field1 降序，field2 升序。
 */
@Schema(description = "可排序的分页参数")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SortablePageParam extends PageParam {

    /**
     * 序列化版本标识
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 排序字段字符串
     * <p>
     * 格式说明：
     * <ul>
     *   <li>多个字段使用逗号分隔，例如：{@code field1,-field2,field3}</li>
     *   <li>字段前加 {@code -} 表示降序排列，不加则表示升序排列</li>
     *   <li>示例：{@code -createTime,id} 表示先按创建时间降序，再按 ID 升序</li>
     * </ul>
     */
    @Schema(description = "排序字段，格式：-field1,field2", example = "-createTime,id")
    private String sort;

    /**
     * 自动解析 {@code sort} 字符串为排序对象列表
     * <p>
     * 解析规则：
     * <ol>
     *   <li>若 {@code sort} 为空或空白，返回空列表</li>
     *   <li>按逗号分割字符串，遍历每个字段</li>
     *   <li>若字段以 {@code -} 开头，则创建降序的 {@link SortingField} 对象</li>
     *   <li>否则，创建升序的 {@link SortingField} 对象</li>
     * </ol>
     *
     * @return 排序字段对象列表，若未指定排序则返回空列表
     */
    public List<SortingField> getSortingFields() {
        if (StrUtil.isBlank(sort)) {
            return Collections.emptyList();
        }
        return Arrays.stream(sort.split(","))
                .map(field -> {
                    boolean ascending = !field.startsWith("-");
                    String fieldName = ascending ? field : field.substring(1);
                    return new SortingField(fieldName, ascending);
                })
                .toList();
    }
}
