package me.link.bootstrap.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.link.bootstrap.core.enums.StatusEnum;
import me.link.bootstrap.core.exception.GlobalException;
import me.link.bootstrap.core.pojo.SortablePageParam;
import me.link.bootstrap.core.pojo.SortingField;
import me.link.bootstrap.system.controller.vo.TenantExpiryRespVO;
import me.link.bootstrap.system.dal.domain.TenantDO;
import me.link.bootstrap.system.dal.enums.ExpiredEnum;
import me.link.bootstrap.system.dal.mapper.TenantMapper;
import me.link.bootstrap.system.service.TenantService;
import me.link.bootstrap.util.SystemClockUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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

        LocalDateTime now = SystemClockUtils.localDateTime();
        LocalDateTime expireTime = tenant.getExpireTime();

        // 1. 判断是否过期
        boolean isExpired = expireTime != null && now.isAfter(expireTime);

        // 2. 计算剩余天数 (使用 ChronoUnit)
        long days = expireTime != null ? ChronoUnit.DAYS.between(now, expireTime) : 0;

        return TenantExpiryRespVO.builder()
                .isExpired(isExpired)
                .remainingDays(Math.max(0, days))
                .expireTime(expireTime)
                .contractStatus(isExpired ? ExpiredEnum.EXPIRED : ExpiredEnum.IN_FORCE)
                .build();
    }

    /**
     * 分页搜索租户信息
     *
     * @param param 可排序的分页参数，包含分页信息和排序字段配置
     *              <ul>
     *                  <li>sort: 排序字段字符串，多个字段使用逗号分隔，字段前加 - 表示降序</li>
     *                  <li>示例：-createTime,id 表示先按创建时间降序，再按 ID 升序</li>
     *              </ul>
     * @return {@link IPage}<{@link TenantDO}> 包含租户数据的分页结果，每页包含租户的详细信息
     */
    @Override
    public IPage<TenantDO> searchByPage(SortablePageParam param) {

        List<SortingField> sorts = param.getSortingFields();
        QueryWrapper<TenantDO> queryWrapper = new QueryWrapper<>();
        if (CollUtil.isNotEmpty(sorts)) {
            sorts.forEach(sort ->
                    queryWrapper.orderBy(true, sort.isAsc(), sort.getField())
            );
        }

        return page(new Page<>(param.getPageNo(), param.getPageSize()), queryWrapper);
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
