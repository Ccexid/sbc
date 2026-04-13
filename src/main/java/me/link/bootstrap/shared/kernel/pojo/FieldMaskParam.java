package me.link.bootstrap.shared.kernel.pojo;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Schema(description = "更新操作继承对象")
public class FieldMaskParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "更新掩码：指定需要更新的字段路径，多个以逗号分隔", example = "name,status,expire_time")
    private String updateMask;

    /**
     * 获取更新掩码字段集合
     *
     * @return 更新字段名称的 Set 集合，如果 updateMask 为空或空白则返回空集合
     */
    public Set<String> getMaskSet() {
        if (StrUtil.isBlank(updateMask)) {
            return Collections.emptySet();
        }
        return Arrays.stream(updateMask.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }
}
