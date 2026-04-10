package me.link.bootstrap.core.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import me.link.bootstrap.core.annotation.LogField;
import me.link.bootstrap.core.pojo.FieldMaskParam;
import org.apache.ibatis.type.JdbcType;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import static me.link.bootstrap.core.constants.GlobalApiConstants.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public abstract class BaseDO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @LogField(value = "创建时间")
    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @LogField(value = "更新时间")
    private LocalDateTime updateTime;
    /**
     * 创建者，目前使用 SysUser 的 id 编号
     * <p>
     */
    @TableField(fill = FieldFill.INSERT, jdbcType = JdbcType.VARCHAR)
    @LogField(value = "创建者")
    private Long creator;
    /**
     * 更新者，目前使用 SysUser 的 id 编号
     * <p>
     */
    @TableField(fill = FieldFill.INSERT_UPDATE, jdbcType = JdbcType.VARCHAR)
    @LogField(value = "更新者")
    private Long updater;
    /**
     * 是否删除
     */
    @TableLogic
    @LogField(value = "是否删除")
    private Boolean deleted;
}
