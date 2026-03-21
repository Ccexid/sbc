package me.link.bootstrap.core.log.spi;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

@Slf4j
public class AuditLogStorageProvider {

    private static final List<AuditLogStorage> CACHED_STORAGES;

    static {
        log.info(">>> 正在预加载审计日志 SPI 实现类...");
        List<AuditLogStorage> storages = new ArrayList<>();
        // 类加载时仅扫描一次
        ServiceLoader<AuditLogStorage> loader = ServiceLoader.load(AuditLogStorage.class);
        loader.forEach(storages::add);
        CACHED_STORAGES = Collections.unmodifiableList(storages);
    }

    public static List<AuditLogStorage> getStorages() {
        return CACHED_STORAGES;
    }
}