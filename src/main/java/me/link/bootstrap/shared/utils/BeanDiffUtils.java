package me.link.bootstrap.shared.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.shared.kernel.annotation.LogField;
import me.link.bootstrap.shared.utils.audit.FieldChange;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体类差异比较工具 (最终加固版)
 * <p>
 * 解决缓存抖动、日志堆栈丢失及格式化性能问题。
 */
@Slf4j
public final class BeanDiffUtils {

    /**
     * 线程安全的字段反射缓存映射表。
     * Key为已剥离代理的真实业务类，Value为该类中标记了@LogField注解的字段列表。
     * 初始容量设置为128以减少扩容次数。
     */
    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>(128);

    /**
     * 字段值字符串最大长度限制。
     * 超过此长度的值将被截断并追加"..."，防止日志文件过大。
     */
    private static final int MAX_VAL_LENGTH = 500;

    private BeanDiffUtils() {
    }

    /**
     * 比较两个对象的差异，提取标记了@LogField注解且值发生变化的字段。
     * <p>
     * 该方法会自动剥离Spring/CGLIB/Hibernate等代理类，确保对比的是真实业务对象。
     * 仅当两个对象类型完全一致时才会进行对比，否则返回空列表。
     *
     * @param oldObj 变更前的对象，可为null
     * @param newObj 变更后的对象，可为null
     * @return 字段变更详情列表；如果任一对象为null、类型不匹配或无变化字段，则返回空列表
     */
    public static List<FieldChange> diff(Object oldObj, Object newObj) {
        if (oldObj == null || newObj == null) {
            return Collections.emptyList();
        }

        // 获取用户定义的真实业务类，自动剥离各类代理包装
        Class<?> oldClass = ClassUtils.getUserClass(oldObj);
        Class<?> newClass = ClassUtils.getUserClass(newObj);

        // 严格类型校验：确保对比的是同一业务类的实例
        if (oldClass != newClass) {
            log.warn("BeanDiffUtils: Type mismatch. Old: [{}], New: [{}]",
                    oldClass.getName(), newClass.getName());
            return Collections.emptyList();
        }

        List<Field> annotatedFields = getCachedFields(oldClass);
        if (annotatedFields.isEmpty()) {
            return Collections.emptyList();
        }

        // 预分配ArrayList容量，避免动态扩容带来的性能损耗
        List<FieldChange> details = new ArrayList<>(annotatedFields.size());

        for (Field field : annotatedFields) {
            try {
                Object oldValue = ReflectUtil.getFieldValue(oldObj, field);
                Object newValue = ReflectUtil.getFieldValue(newObj, field);

                if (ObjectUtil.notEqual(oldValue, newValue)) {
                    LogField ann = field.getAnnotation(LogField.class);
                    if (ann == null) {
                        continue;
                    }
                    // 优先使用注解配置的字段描述，为空时降级使用字段名
                    String fieldDesc = ann.value().isBlank() ? field.getName() : ann.value();

                    details.add(new FieldChange(
                            fieldDesc,
                            formatValue(oldValue),
                            formatValue(newValue)
                    ));
                }
            } catch (Exception e) {
                // 记录完整异常堆栈便于问题定位，使用warn级别避免影响主业务流程
                log.warn("BeanDiffUtils: Field comparison failed for [{}.{}]",
                        oldClass.getSimpleName(), field.getName(), e);
            }
        }

        return details;
    }

    /**
     * 从缓存中获取指定类的@LogField标注字段列表，若不存在则通过反射发现并缓存。
     * <p>
     * 利用ConcurrentHashMap的computeIfAbsent方法保证并发环境下的原子性操作，
     * 避免多线程重复反射扫描同一类。
     *
     * @param clazz 需要获取字段的真实业务类（非代理类）
     * @return 不可变的@LogField字段列表，若无标注字段则返回空列表
     */
    private static List<Field> getCachedFields(Class<?> clazz) {
        // 直接利用 computeIfAbsent 的原子性，简化逻辑并避免并发竞态
        // ConcurrentHashMap 在 JDK 8+ 中对 computeIfAbsent 有高度优化
        return FIELD_CACHE.computeIfAbsent(clazz, BeanDiffUtils::findLogFields);
    }

    /**
     * 通过反射扫描指定类及其父类中所有标记了@LogField注解的字段。
     * <p>
     * 对发现的字段预先设置accessible=true，提升后续反射读取性能。
     * 返回不可变列表以防止外部修改缓存数据。
     *
     * @param clazz 待扫描的业务类
     * @return 包含@LogField注解的字段列表；若无匹配字段则返回空列表
     */
    private static List<Field> findLogFields(Class<?> clazz) {
        Field[] allFields = ReflectUtil.getFields(clazz);
        List<Field> logFields = new ArrayList<>();
        for (Field field : allFields) {
            if (field.isAnnotationPresent(LogField.class)) {
                ReflectUtil.setAccessible(field);
                logFields.add(field);
            }
        }
        return logFields.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(logFields);
    }

    /**
     * 将对象值格式化为适合日志输出的字符串表示。
     * <p>
     * 支持以下特性：
     * <ul>
     *   <li>null值转换为空字符串</li>
     *   <li>数组、集合、Map使用Hutool工具格式化以提升可读性</li>
     *   <li>普通对象直接使用String.valueOf</li>
     *   <li>超长字符串自动截断并追加省略号</li>
     * </ul>
     *
     * @param value 待格式化的对象值，可为null
     * @return 格式化后的字符串；null值返回空字符串；超长字符串截断至500字符
     */
    private static String formatValue(Object value) {
        if (value == null) {
            return "";
        }

        String str;
        // 针对数组、集合、Map 使用 Hutool 的 toString 以获得可读性更强的输出
        // 对于普通对象，直接使用 String.valueOf 以减少开销
        if (value.getClass().isArray() || value instanceof Collection || value instanceof Map) {
            str = ObjectUtil.toString(value);
        } else {
            str = String.valueOf(value);
        }

        if (str.length() > MAX_VAL_LENGTH) {
            return str.substring(0, MAX_VAL_LENGTH - 3) + "...";
        }
        return str;
    }
}