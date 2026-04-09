package me.link.bootstrap.core.mybatis.interceptor;

import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.core.annotation.IdGenerator;
import me.link.bootstrap.util.IdUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis ID自动生成拦截器（生产环境严谨版）
 * <p>
 * 核心功能：
 * 1. 拦截MyBatis的INSERT操作，自动为标注了@IdGenerator注解的字段生成唯一ID
 * 2. 支持多种参数类型：单对象、集合、数组、Map
 * 3. 使用ThreadLocal实现线程隔离，防止高并发下的重复处理
 * 4. 反射字段缓存机制，提升性能
 * 5. 支持继承体系，自动扫描父类字段
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * @IdGenerator(prefix = "ORD", digit = 6, daily = true)
 * private String orderId;
 * }</pre>
 * </p>
 *
 * @author Link Team
 * @see IdGenerator
 * @see IdUtils
 */
@Component
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class IdGeneratorInterceptor implements Interceptor {

    /**
     * 字段缓存：存储每个类中标注了@IdGenerator的字段列表
     * Key: 实体类Class对象
     * Value: 需要自动生成ID的字段列表（不可变）
     * <p>
     * 使用ConcurrentHashMap保证线程安全，computeIfAbsent保证原子性
     * </p>
     */
    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * ThreadLocal存储当前线程已处理的对象集合，用于防止循环引用导致的重复处理
     * <p>
     * 核心设计：
     * 1. 使用IdentityHashMap基于对象引用（而非equals）进行去重
     * 2. 每个线程独立的Set，避免高并发下的竞争
     * 3. 必须在finally块中清理，防止Tomcat线程池内存泄漏
     * </p>
     */
    private static final ThreadLocal<Set<Object>> PROCESSED_HOLDER = ThreadLocal.withInitial(() ->
            Collections.newSetFromMap(new IdentityHashMap<>()));

    /**
     * MyBatis拦截器核心方法：拦截Executor.update()调用
     * <p>
     * 拦截逻辑：
     * 1. 仅处理INSERT操作，其他SQL命令直接放行
     * 2. 对INSERT操作的参数进行ID自动填充
     * 3. 使用try-finally确保ThreadLocal资源正确清理
     * </p>
     *
     * @param invocation MyBatis拦截器调用上下文，包含目标方法和参数
     * @return 原始方法的执行结果
     * @throws Throwable 执行过程中的异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        
        // 仅拦截INSERT操作，其他操作（UPDATE/DELETE）直接放行
        if (ms.getSqlCommandType() != SqlCommandType.INSERT) {
            return invocation.proceed();
        }

        Object parameter = invocation.getArgs()[1];
        
        // 参数为空时直接放行
        if (parameter == null) {
            return invocation.proceed();
        }

        try {
            // 处理参数，自动填充ID字段
            processParameter(parameter);
        } finally {
            // 必须清理ThreadLocal，防止Tomcat线程池复用导致的内存泄漏和数据污染
            PROCESSED_HOLDER.get().clear();
            PROCESSED_HOLDER.remove();
        }

        // 继续执行原始的INSERT操作
        return invocation.proceed();
    }

    /**
     * 递归处理参数对象，支持多种数据类型
     * <p>
     * 处理流程：
     * 1. 空值检查
     * 2. 获取当前线程的去重集合
     * 3. 剥离代理对象，获取真实对象
     * 4. 去重检查，防止循环引用导致的重复处理
     * 5. 根据参数类型分发处理：
     *    - Collection: 遍历每个元素填充ID
     *    - Array: 遍历数组元素填充ID
     *    - Map: 递归处理Map中的所有值
     *    - 普通对象: 直接填充ID字段
     * </p>
     *
     * @param param 待处理的参数对象，可以是单对象、集合、数组或Map
     */
    private void processParameter(Object param) {
        if (param == null) return;

        // 获取当前线程的去重集合
        Set<Object> processed = PROCESSED_HOLDER.get();

        // 剥离MyBatis/CGLIB代理，获取真实对象
        Object realParam = param;
        if (!(param instanceof Collection || param instanceof Map || param.getClass().isArray())) {
            realParam = SystemMetaObject.forObject(param).getOriginalObject();
        }

        // 去重检查：如果该对象已处理过，直接返回，防止循环引用
        if (!processed.add(realParam)) {
            return;
        }

        // 处理集合类型：批量插入场景
        if (realParam instanceof Collection<?> collection) {
            collection.forEach(this::fillIdValue);
            return;
        }

        // 处理数组类型：兼容数组形式的批量插入
        if (realParam.getClass().isArray()) {
            int length = Array.getLength(realParam);
            for (int i = 0; i < length; i++) {
                fillIdValue(Array.get(realParam, i));
            }
            return;
        }

        // 处理Map类型：递归处理Map中的所有值
        if (realParam instanceof Map<?, ?> map) {
            map.values().forEach(this::processParameter);
            return;
        }

        // 处理单个对象：直接填充ID字段
        fillIdValue(realParam);
    }

    /**
     * 为实体对象中的@IdGenerator标注字段自动填充ID值
     * <p>
     * 处理逻辑：
     * 1. 空值和基础类型过滤（基本类型、枚举、Java标准库类）
     * 2. 从缓存中获取该类的所有@IdGenerator字段
     * 3. 遍历字段，仅当字段值为null时才生成ID
     * 4. 调用IdUtils生成符合规则的分布式ID
     * 5. 通过反射设置字段值
     * </p>
     * <p>
     * 异常处理：捕获所有异常并记录日志，避免ID生成失败影响主业务流程
     * </p>
     *
     * @param entity 待填充ID的实体对象
     */
    private void fillIdValue(Object entity) {
        if (entity == null) return;

        Class<?> clazz = entity.getClass();
        
        // 过滤不需要处理ID的类型：基本类型、枚举、Java/Javax标准库类
        if (clazz.isPrimitive() || clazz.isEnum() || clazz.getName().startsWith("java.")
                || clazz.getName().startsWith("javax.")) {
            return;
        }

        // 从缓存中获取标注了@IdGenerator的字段列表
        List<Field> fields = getAnnotatedFields(clazz);
        
        // 遍历所有需要生成ID的字段
        for (Field field : fields) {
            try {
                // 仅当字段值为null时才生成ID（允许手动指定ID）
                if (field.get(entity) == null) {
                    // 获取字段的@IdGenerator注解配置
                    IdGenerator idGen = field.getAnnotation(IdGenerator.class);
                    
                    // 调用分布式ID生成器生成唯一ID
                    String nextId = IdUtils.next(idGen.prefix(), idGen.digit(), idGen.daily());
                    
                    // 通过反射设置生成的ID值
                    field.set(entity, nextId);
                    
                    log.debug("ID自动填充成功：{}.{} -> {}", clazz.getSimpleName(), field.getName(), nextId);
                }
            } catch (Exception e) {
                // 记录异常但不抛出，避免影响主业务流程
                log.error("ID自动填充异常，类：{}，字段：{}", clazz.getName(), field.getName(), e);
            }
        }
    }

    /**
     * 获取类中所有标注了@IdGenerator注解的字段（含父类）
     * <p>
     * 核心特性：
     * 1. 使用ConcurrentHashMap.computeIfAbsent实现懒加载和线程安全
     * 2. 向上遍历继承链，支持从父类继承的字段
     * 3. 排除静态字段（static），仅处理实例字段
     * 4. 自动设置字段可访问性（setAccessible=true）
     * 5. 返回不可变列表，防止外部修改缓存
     * </p>
     * <p>
     * 性能优化：
     * - 首次访问时扫描并缓存，后续直接从缓存读取
     * - 避免重复的反射操作，显著提升高并发场景性能
     * </p>
     *
     * @param clazz 目标实体类
     * @return 标注了@IdGenerator的字段列表（不可变）
     */
    private List<Field> getAnnotatedFields(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, c -> {
            List<Field> annotatedFields = new ArrayList<>();
            Class<?> tempClass = c;
            
            // 向上遍历继承链，直到Object类
            while (tempClass != null && tempClass != Object.class) {
                // 扫描当前类声明的所有字段
                for (Field field : tempClass.getDeclaredFields()) {
                    // 排除静态字段，且必须标注@IdGenerator注解
                    if (!Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(IdGenerator.class)) {
                        // 设置字段可访问，绕过private/protected限制
                        field.setAccessible(true);
                        annotatedFields.add(field);
                    }
                }
                // 继续检查父类
                tempClass = tempClass.getSuperclass();
            }
            
            // 返回不可变列表，保护缓存数据
            return Collections.unmodifiableList(annotatedFields);
        });
    }

    /**
     * MyBatis插件包装方法：创建代理对象
     * <p>
     * 将当前拦截器包装到目标对象中，使MyBatis能够在执行时触发拦截逻辑
     * </p>
     *
     * @param target 被拦截的目标对象（通常是Executor实现类）
     * @return 包装后的代理对象
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
}