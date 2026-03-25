package me.link.bootstrap.framework.auth;

import cn.dev33.satoken.model.wrapperInfo.SaDisableWrapperInfo;
import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限认证接口实现类
 * 用于自定义用户权限、角色获取逻辑以及禁用状态检查
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 获取指定用户的权限码列表
     * <p>
     * 步骤说明：
     * 1. 接收参数：o (用户 ID), s (登录类型)
     * 2. 根据业务逻辑查询该用户拥有的所有权限码
     * 3. 返回权限码字符串列表
     * </p>
     *
     * @param o 用户 ID，通常为字符串或数字类型
     * @param s 登录类型，用于区分不同端的登录（如 "PC", "APP" 等）
     * @return 权限码列表，当前实现返回空列表，需根据实际业务补充查询逻辑
     */
    @Override
    public List<String> getPermissionList(Object o, String s) {
        // TODO: 此处应添加从数据库或缓存中查询用户权限的逻辑
        // 示例：return permissionService.findByUserId(o.toString());
        return List.of();
    }


    /**
     * 获取指定用户的角色码列表
     * <p>
     * 步骤说明：
     * 1. 接收参数：o (用户 ID), s (登录类型)
     * 2. 根据业务逻辑查询该用户关联的所有角色码
     * 3. 返回角色码字符串列表
     * </p>
     *
     * @param o 用户 ID，通常为字符串或数字类型
     * @param s 登录类型，用于区分不同端的登录
     * @return 角色码列表，当前实现返回空列表，需根据实际业务补充查询逻辑
     */
    @Override
    public List<String> getRoleList(Object o, String s) {
        // TODO: 此处应添加从数据库或缓存中查询用户角色的逻辑
        // 示例：return roleService.findByUserId(o.toString());
        return List.of();
    }


    /**
     * 检查指定用户是否被禁用
     * <p>
     * 步骤说明：
     * 1. 接收参数：loginId (用户登录 ID), service (服务标识)
     * 2. 调用父类默认实现进行禁用状态检查
     * 3. 返回包含禁用信息的包装对象，若未禁用则返回 null 或特定状态对象
     * </p>
     *
     * @param loginId 用户登录 ID
     * @param service 服务标识，用于区分不同服务的禁用策略
     * @return 禁用包装信息，默认调用父类实现，可根据需求重写自定义禁用逻辑
     */
    @Override
    public SaDisableWrapperInfo isDisabled(Object loginId, String service) {
        // 默认使用 Sa-Token 提供的标准禁用检查逻辑
        return StpInterface.super.isDisabled(loginId, service);
    }
}
