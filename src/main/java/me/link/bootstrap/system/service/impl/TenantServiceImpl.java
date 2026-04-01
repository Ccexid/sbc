package me.link.bootstrap.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.link.bootstrap.core.enums.StatusEnum;
import me.link.bootstrap.core.exception.GlobalException;
import me.link.bootstrap.system.controller.vo.TenantExpiryRespVO;
import me.link.bootstrap.system.dal.domain.TenantDO;
import me.link.bootstrap.system.dal.enums.ExpiredEnum;
import me.link.bootstrap.system.dal.mapper.TenantMapper;
import me.link.bootstrap.system.service.TenantService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 租户服务实现类
 */
@Service
public class TenantServiceImpl extends ServiceImpl<TenantMapper, TenantDO> implements TenantService {

    /**
     * 检查租户是否过期并返回相关状态信息
     *
     * @param id 租户 ID
     * @return 租户过期响应信息，包含是否过期、剩余天数、到期时间、合同状态及状态描述
     */
    @Override
    public TenantExpiryRespVO isExpired(Long id) {
        // 获取租户信息（包含状态校验）
        TenantDO tenant = this.getByIdWithFallback(id);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = tenant.getExpireTime();

        // 1. 判断是否过期
        boolean isExpired = expireTime != null && now.isAfter(expireTime);

        // 2. 计算剩余天数 (使用 ChronoUnit)
        long days = 0;
        if (expireTime != null && !isExpired) {
            days = ChronoUnit.DAYS.between(now, expireTime);
        }

        return TenantExpiryRespVO.builder()
                .isExpired(isExpired)
                .remainingDays(Math.max(0, days))
                .expireTime(expireTime)
                .contractStatus(isExpired ? ExpiredEnum.EXPIRED : ExpiredEnum.IN_FORCE)
                .build();
    }

    /**
     * 根据 ID 获取租户信息（带降级处理）
     * <p>
     * 查询条件：
     * - 匹配指定 ID
     * - 状态必须为正常 (StatusEnum.NORMAL)
     * </p>
     *
     * @param id 租户 ID
     * @return 租户实体对象
     * @throws GlobalException 当查询结果为空时抛出“租户不存在”异常
     */
    private TenantDO getByIdWithFallback(Long id) {
        // 构建查询条件：ID 匹配且状态正常
        TenantDO tenant = this.getOne(
                Wrappers.<TenantDO>lambdaQuery()
                        .eq(TenantDO::getId, id)
                        .eq(TenantDO::getStatus, StatusEnum.NORMAL)
        );

        // 若未找到有效租户，抛出全局异常
        if (ObjectUtil.isEmpty(tenant)) {
            throw new GlobalException("租户不存在，请联系平台方确认");
        }

        return tenant;
    }
}
