package me.link.bootstrap.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型枚举
 * <p>
 * 定义系统中支持的各种操作类型，用于记录用户操作日志和审计追踪。
 * </p>
 *
 * @author link
 */
@Getter
@AllArgsConstructor
public enum OperationEnum {
    /**
     * 基础操作 (0-9)
     */
    CREATED(0, "创建"),
    UPDATED(1, "更新"),
    VIEWED(2, "查看"),
    DELETED(3, "删除"),

    /**
     * 数据交换 (10-19)
     */
    EXPORTED(10, "导出"),
    IMPORTED(11, "导入"),
    UPLOADED(12, "上传"),

    /**
     * 认证安全 (20-29)
     */
    LOGIN(20, "登录"),
    LOGOUT(21, "登出"),
    GRANT(22, "权限分配"),

    /**
     * 业务流程 (30-39)
     */
    PAYED(30, "支付"),
    APPROVE(31, "审批通过"),
    REJECT(32, "审批驳回"),

    /**
     * 其他系统操作 (90-99)
     */
    CLEAN(90, "清理"),
    OTHER(99, "其他操作");

    /**
     * 状态值（存储到数据库）
     */
    @EnumValue // 标记数据库存的值
    private final Integer value;

    /**
     * 状态描述（返回给前端）
     */
    @JsonValue  // 标记返回给前端的值（如果你想返回"正常"而不是 0）
    private final String desc;
}
