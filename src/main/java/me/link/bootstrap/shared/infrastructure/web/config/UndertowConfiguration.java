package me.link.bootstrap.shared.infrastructure.web.config;

import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UndertowConfiguration implements WebServerFactoryCustomizer<UndertowServletWebServerFactory> {

    @Override
    public void customize(UndertowServletWebServerFactory factory) {
        factory.addBuilderCustomizers(builder -> {
            // 1. IO 线程数：处理 TCP 连接。通常设置为 CPU 核心数。
            // 对于 8 核机器，建议设为 8。过高会增加线程切换开销。
            builder.setIoThreads(Runtime.getRuntime().availableProcessors());

            // 2. Worker 线程数：处理具体业务逻辑（如数据库操作）。
            // 建议公式：IO 线程数 * 8。高并发业务可适当调大。
            builder.setWorkerThreads(Runtime.getRuntime().availableProcessors() * 8);

            // 3. 设置连接超时间（毫秒），防止大批死连接占用资源
            builder.setServerOption(io.undertow.UndertowOptions.NO_REQUEST_TIMEOUT, 30000);

            // 4. 开启 HTTP 保持连接 (Keep-Alive)
            builder.setServerOption(io.undertow.UndertowOptions.ENABLE_HTTP2, true);
        });

        factory.addDeploymentInfoCustomizers(deploymentInfo -> {
            // 5. 彻底解决 Buffer Pool 警告，并根据内存情况调优
            // direct: true 使用堆外内存（减少 GC 压力），bufferSize: 16KB 是高性能推荐值
            WebSocketDeploymentInfo wsInfo = new WebSocketDeploymentInfo();
            wsInfo.setBuffers(new DefaultByteBufferPool(true, 16384));
            deploymentInfo.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, wsInfo);
        });
    }
}