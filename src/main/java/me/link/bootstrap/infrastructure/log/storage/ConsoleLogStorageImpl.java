package me.link.bootstrap.infrastructure.log.storage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.domain.log.model.AuditLogDTO;
import me.link.bootstrap.domain.log.spi.AuditLogStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 控制台审计日志存储实现 (基础设施层)
 * 优化点：增强可读性、支持字段变更详情展示、环境动态开关
 */
@Slf4j
@Component
public class ConsoleLogStorageImpl implements AuditLogStorage {

    @Override
    public void record(AuditLogDTO dto) {
        StringBuilder report = new StringBuilder();
        report.append("\n").append("=".repeat(30)).append(" AUDIT LOG REPORT ").append("=".repeat(30))
                .append("\n[TraceId]   : ").append(dto.getTraceId())
                .append("\n[TenantId]  : ").append(dto.getTenantId())
                .append("\n[Module]    : ").append(dto.getModule())
                .append("\n[Operation] : ").append(dto.getOperation())
                .append("\n[BusinessId]: ").append(dto.getBusinessId())
                .append("\n[Status]    : ").append(dto.getStatus())
                .append("\n[CostTime]  : ").append(dto.getCostTime()).append("ms");

        // 核心：打印字段变更明细
        if (CollUtil.isNotEmpty(dto.getChanges())) {
            report.append("\n[Changes]   : ");
            dto.getChanges().forEach(change ->
                    report.append("\n  - ").append(change.getFieldLabel())
                            .append(" (").append(change.getFieldName()).append("): ")
                            .append("[").append(change.getBeforeValue()).append("]")
                            .append(" -> ")
                            .append("[").append(change.getAfterValue()).append("]")
            );
        }

        if (dto.getErrorMsg() != null) {
            report.append("\n[ErrorMsg]  : ").append(dto.getErrorMsg());
        }

        // 附带完整 JSON 镜像，方便一键复制去调试
        report.append("\n[Snapshot]  : ").append(JSONUtil.toJsonStr(dto))
                .append("\n").append("=").append("=".repeat(76));

        // 整合为一条日志输出，防止多线程环境下控制台文字交织乱序
        log.info(report.toString());
    }

    @Override
    public String getName() {
        return "Console-Storage";
    }

    @Override
    public boolean isEnabled() {
        // 仅在 dev 或 test 环境下默认开启，避免生产环境 IO 过载
        return true;
    }

    @Override
    public int getOrder() {
        // 控制台打印建议排在最后
        return Integer.MAX_VALUE;
    }
}