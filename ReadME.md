# ReadME

## Descriptions

```text
src/
├── main/
│   ├── java/me/link/bootstrap/
│   │   /* ==================== 1. CORE BUSINESS LAYERS (核心业务四层) ==================== */
│   │   ├── interfaces/                     // 用户接口层：只负责协议转换、参数校验、分发
│   │   │   ├── assembler/                  // DTO 与 Domain Entity 的双向转换器
│   │   │   ├── controller/                 // 暴露的 RESTful 接口
│   │   │   └── dto/                        // 接口层的契约数据对象
│   │   │       ├── request/                // 接收参数（如 SortablePageRequest）
│   │   │       └── response/               // 返回参数（如 ResultResponse）
│   │   │
│   │   ├── application/                    // 应用层：编排领域服务、处理事务、安全检查
│   │   │   ├── dto/                        // 应用层内部流转的命令对象 (Command/Query)
│   │   │   └── service/                    // 应用服务实现
│   │   │
│   │   ├── domain/                         // 领域层：纯净的业务核心，绝不依赖具体的数据库/技术
│   │   │   ├── aggregate/                  // 聚合根
│   │   │   ├── entity/                     // 领域实体
│   │   │   ├── event/                      // 领域事件定义
│   │   │   ├── factory/                    // 复杂实体的构建工厂
│   │   │   ├── repository/                 // 仓储接口（注意：这里只有接口声明！）
│   │   │   ├── service/                    // 无法归属于单一实体的领域服务流
│   │   │   └── valueobject/                // 领域值对象
│   │   │
│   │   ├── infrastructure/                 // 基础设施层：为上面三层提供技术实现支撑
│   │   │   ├── adapter/                    // 外部三方服务调用适配（如 RPC、短信）
│   │   │   ├── exception/                  // 💡 调整：全局异常拦截（@RestControllerAdvice）移至此处
│   │   │   ├── messaging/                  // 消息队列生产者/消费者实现
│   │   │   └── persistence/                // 数据库持久化
│   │   │       ├── converter/              // DO 与 Entity 的 MapStruct 转换
│   │   │       ├── mapper/                 // 💡 显式增加：MyBatis-Plus 的 Mapper 接口
│   │   │       ├── po/                     // 数据库持久化对象 (Persistent Object)
│   │   │       └── repository/             // 仓储实现类（如 LinkRepositoryImpl，实现 domain 里的接口）
│   │   │
│   │   /* ==================== 2. TECHNICAL SHARED KERNEL (通用脚手架技术底座) ==================== */
│   │   ├── shared/kernel/                  // 💡 纯粹的技术内核包（完全无具体业务语义，只提供抽象与基类）
│   │   │   ├── component/                  // 通用技术组件封装（如分布式布隆过滤器、安全加解密）
│   │   │   ├── constant/                   // 全局核心技术常量（GlobalConstants）
│   │   │   ├── converter/                  // MapStruct 的通用基类（BaseConverter）
│   │   │   ├── exception/                  // 全局错误码定义（ErrorCode、BusinessException 基类）
│   │   │   ├── valueobject/                // 基础技术值对象（SortingField）
│   │   │   │
│   │   │   // 💡 调整：将零散的自动配置和拦截器，按技术组件内聚收拢到此处
│   │   │   ├── database/mybatis/           
│   │   │   │   ├── config/
│   │   │   │   │   └── LinkMybatisAutoConfiguration.java
│   │   │   │   ├── handler/
│   │   │   │   │   └── LinkDefaultDBFieldHandler.java
│   │   │   │   ├── BaseDO.java
│   │   │   │   └── TenantBaseDO.java
│   │   │   ├── jackson/
│   │   │   │   ├── config/
│   │   │   │   │   └── LinkJacksonAutoConfiguration.java
│   │   │   │   └── deserializer/
│   │   │   │       └── XssStringDeserializer.java
│   │   │   ├── web/
│   │   │   │   └── config/
│   │   │   │       ├── LinkCorsAutoConfiguration.java
│   │   │   │       └── LinkUndertowAutoConfiguration.java
│   │   │   └── tracing/
│   │   │       ├── config/
│   │   │       │   └── LinkTracingAutoConfiguration.java // 建议用自动配置托管
│   │   │       ├── TraceIdContext.java
│   │   │       └── TraceIdFilter.java
│   │   │
│   │   └── LinkMainApplication.java        // 启动类
│   │
│   └── resources/
│       ├── META-INF/spring/
│       │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports // 💡 里面依然托管已经移入 shared 里的配置类
```