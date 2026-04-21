package me.link.bootstrap.shared.util.audit;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.shared.kernel.annotation.LogField;
import me.link.bootstrap.shared.kernel.constant.GlobalConstants;
import org.springframework.util.ClassUtils;

import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 实体类差异比较工具 (最终加固版)
 * <p>
 * 解决缓存抖动、日志堆栈丢失及格式化性能问题。
 */
@Slf4j
public final class BeanDiffUtil {

    private static final int MAX_VAL_LENGTH = 500;
    private static final int MAX_COLLECTION_SIZE = 10;
    private static final int MAX_OBJECT_DEPTH = 3;

    // 使用Caffeine缓存 + 软引用
    private static final Cache<Class<?>, SoftReference<List<Field>>> FIELD_CACHE =
            Caffeine.newBuilder()
                    .maximumSize(200)
                    .expireAfterAccess(1, TimeUnit.HOURS)
                    .build();

    private static final ThreadLocal<IdentityHashMap<Object, Integer>> VISITED_OBJECTS =
            ThreadLocal.withInitial(IdentityHashMap::new);

    public static List<FieldChange> diff(Object oldObj, Object newObj) {
        if (oldObj == null || newObj == null) {
            return Collections.emptyList();
        }

        Class<?> oldClass = ClassUtils.getUserClass(oldObj);
        Class<?> newClass = ClassUtils.getUserClass(newObj);

        if (oldClass != newClass) {
            log.debug("类型不匹配，跳过diff: {} vs {}",
                    oldClass.getSimpleName(), newClass.getSimpleName());
            return Collections.emptyList();
        }

        List<Field> annotatedFields = getCachedFields(oldClass);
        if (annotatedFields.isEmpty()) {
            return Collections.emptyList();
        }

        List<FieldChange> details = new ArrayList<>(annotatedFields.size());
        IdentityHashMap<Object, Integer> visited = VISITED_OBJECTS.get();
        visited.clear();  // 确保每次diff前清理

        try {
            for (Field field : annotatedFields) {
                processField(oldObj, newObj, field, details);
            }
        } finally {
            visited.clear();  // 确保清理
        }
        return details;
    }

    /**
     * 从缓存中获取字段列表，使用软引用防止内存泄漏
     */
    private static List<Field> getCachedFields(Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }

        try {
            // 使用Caffeine缓存 + 软引用
            SoftReference<List<Field>> ref = FIELD_CACHE.get(clazz, k ->
                    new SoftReference<>(findLogFields(k))
            );

            if (ref != null) {
                List<Field> fields = ref.get();
                if (fields != null && !fields.isEmpty()) {
                    return fields;
                }
            }

            // 缓存失效，重新获取
            List<Field> fields = findLogFields(clazz);
            FIELD_CACHE.put(clazz, new SoftReference<>(fields));
            return fields;
        } catch (Exception e) {
            log.error("获取缓存字段失败: {}", clazz.getName(), e);
            return Collections.emptyList();
        }
    }

    private static void processField(Object oldObj, Object newObj, Field field, List<FieldChange> details) {
        try {
            Object oldValue = getFieldValueSafely(oldObj, field);
            Object newValue = getFieldValueSafely(newObj, field);

            if (valuesChanged(oldValue, newValue)) {
                LogField ann = field.getAnnotation(LogField.class);
                String fieldDesc = ann != null && !ann.value().isBlank()
                        ? ann.value()
                        : field.getName();

                details.add(new FieldChange(
                        fieldDesc,
                        safeFormat(oldValue),
                        safeFormat(newValue)
                ));
            }
        } catch (Exception e) {
            log.trace("字段比较失败: {}.{}",
                    oldObj.getClass().getSimpleName(), field.getName(), e);
        }
    }

    private static String safeFormat(Object value) {
        if (value == null) return "";

        try {
            IdentityHashMap<Object, Integer> visited = VISITED_OBJECTS.get();
            return formatObject(value, 0, visited);
        } catch (StackOverflowError e) {
            log.error("格式化对象时发生栈溢出", e);
            return "[FORMAT_ERROR]";
        } catch (Exception e) {
            log.error("格式化对象失败", e);
            return "[ERROR]";
        }
    }

    private static String formatObject(Object obj, int depth, IdentityHashMap<Object, Integer> visited) {
        if (depth > MAX_OBJECT_DEPTH) {
            return "[...]";
        }

        if (obj == null) {
            return "null";
        }

        // 检测循环引用
        if (visited.containsKey(obj)) {
            return "[Circular]";
        }
        visited.put(obj, depth);

        try {
            // 1. 特殊类型处理
            if (obj instanceof Date) {
                return DateUtil.formatDateTime((Date) obj);
            }
            if (obj instanceof Enum<?>) {
                return ((Enum<?>) obj).name();
            }
            if (obj instanceof Boolean || obj instanceof Number) {
                return obj.toString();
            }

            // 2. 集合类型
            if (obj instanceof Collection) {
                return formatCollection((Collection<?>) obj, depth + 1, visited);
            }
            if (obj instanceof Map) {
                return formatMap((Map<?, ?>) obj, depth + 1, visited);
            }
            if (obj.getClass().isArray()) {
                return formatArray(obj, depth + 1, visited);
            }

            // 3. 普通对象
            String str = obj.toString();
            return truncateString(str, MAX_VAL_LENGTH);

        } finally {
            // 仅在当前深度移除，不影响父级
            if (visited.get(obj) == depth) {
                visited.remove(obj);
            }
        }
    }

    /**
     * 安全格式化集合对象，避免大集合导致的性能问题
     */
    private static String formatCollection(Collection<?> collection, int depth, IdentityHashMap<Object, Integer> visited) {
        if (collection == null || collection.isEmpty()) {
            return "[]";
        }

        // 限制显示的元素数量，避免大集合
        int maxSize = Math.min(collection.size(), MAX_COLLECTION_SIZE);
        StringBuilder sb = new StringBuilder("[");

        int count = 0;
        for (Object item : collection) {
            if (count >= maxSize) {
                sb.append("... (").append(collection.size() - count).append(" more)");
                break;
            }

            if (count > 0) {
                sb.append(", ");
            }

            // 递归格式化，但限制深度
            String itemStr = formatObject(item, depth + 1, visited);
            sb.append(truncateString(itemStr, 100)); // 每个元素限制100字符

            if (sb.length() > MAX_VAL_LENGTH - 50) { // 预留空间
                sb.append("...");
                break;
            }
            count++;
        }
        sb.append("]");
        return truncateString(sb.toString(), MAX_VAL_LENGTH);
    }

    /**
     * 安全格式化Map对象，避免大Map导致的性能问题
     */
    private static String formatMap(Map<?, ?> map, int depth, IdentityHashMap<Object, Integer> visited) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }

        // 限制显示的键值对数量
        int maxSize = Math.min(map.size(), MAX_COLLECTION_SIZE);
        StringBuilder sb = new StringBuilder("{");

        int count = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (count >= maxSize) {
                sb.append("... (").append(map.size() - count).append(" more)");
                break;
            }

            if (count > 0) {
                sb.append(", ");
            }

            String keyStr = formatObject(entry.getKey(), depth + 1, visited);
            String valueStr = formatObject(entry.getValue(), depth + 1, visited);

            sb.append(truncateString(keyStr, 50))
                    .append("=")
                    .append(truncateString(valueStr, 100));

            if (sb.length() > MAX_VAL_LENGTH - 50) {
                sb.append("...");
                break;
            }
            count++;
        }
        sb.append("}");
        return truncateString(sb.toString(), MAX_VAL_LENGTH);
    }

    /**
     * 安全格式化数组对象
     */
    private static String formatArray(Object array, int depth, IdentityHashMap<Object, Integer> visited) {
        if (array == null) {
            return "null";
        }

        int length = Array.getLength(array);
        if (length == 0) {
            return "[]";
        }

        // 限制显示的元素数量
        int maxSize = Math.min(length, MAX_COLLECTION_SIZE);
        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < maxSize; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            Object item = Array.get(array, i);
            String itemStr = formatObject(item, depth + 1, visited);
            sb.append(truncateString(itemStr, 100));

            if (sb.length() > MAX_VAL_LENGTH - 50) {
                sb.append("...");
                break;
            }
        }

        if (length > maxSize) {
            sb.append("... (").append(length - maxSize).append(" more)");
        }
        sb.append("]");
        return truncateString(sb.toString(), MAX_VAL_LENGTH);
    }

    /**
     * 截断字符串，避免超长
     */
    private static String truncateString(String str, int maxLength) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * 格式化特殊类型对象
     */
    private static String formatSpecialType(Object obj) {
        if (obj instanceof Date) {
            return DateUtil.formatDateTime((Date) obj);
        }
        if (obj instanceof LocalDateTime) {
            return ((LocalDateTime) obj).format(DateTimeFormatter.ofPattern(GlobalConstants.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND));
        }
        if (obj instanceof Enum<?>) {
            return ((Enum<?>) obj).name();
        }
        if (obj instanceof Boolean || obj instanceof Number) {
            return obj.toString();
        }
        return null;
    }

    /**
     * 安全获取字段值，处理各种异常情况
     */
    private static Object getFieldValueSafely(Object obj, Field field) {
        if (obj == null || field == null) {
            return null;
        }

        try {
            // 优先尝试直接访问
            if (field.canAccess(obj)) {
                return field.get(obj);
            }

            // 尝试设置可访问性
            try {
                field.setAccessible(true);
                return field.get(obj);
            } catch (SecurityException | IllegalAccessException e) {
                // 安全管理器限制，尝试通过ReflectUtil
                try {
                    return ReflectUtil.getFieldValue(obj, field);
                } catch (Exception ex) {
                    log.trace("无法访问字段 {}.{}",
                            obj.getClass().getSimpleName(), field.getName());
                    return null;
                }
            }
        } catch (Exception e) {
            log.trace("获取字段值失败: {}.{}",
                    obj.getClass().getSimpleName(), field.getName(), e);
            return null;
        }
    }

    /**
     * 智能比较两个值是否发生变化，处理各种边界情况
     */
    private static boolean valuesChanged(Object oldValue, Object newValue) {
        // 处理null情况
        if (oldValue == null && newValue == null) {
            return false;
        }
        if (oldValue == null || newValue == null) {
            return true;
        }

        // 处理相同引用
        if (oldValue == newValue) {
            return false;
        }

        // 处理数组
        if (oldValue.getClass().isArray() && newValue.getClass().isArray()) {
            return !Arrays.deepEquals((Object[]) oldValue, (Object[]) newValue);
        }

        // 处理集合
        if (oldValue instanceof Collection<?> oldColl && newValue instanceof Collection<?> newColl) {
            if (oldColl.size() != newColl.size()) {
                return true;
            }
            return !oldColl.containsAll(newColl);
        }

        // 处理Map
        if (oldValue instanceof Map<?, ?> oldMap && newValue instanceof Map<?, ?> newMap) {
            if (oldMap.size() != newMap.size()) {
                return true;
            }
            return !oldMap.entrySet().containsAll(newMap.entrySet());
        }

        // 处理日期类型
        if (oldValue instanceof Date && newValue instanceof Date) {
            return ((Date) oldValue).getTime() != ((Date) newValue).getTime();
        }
        if (oldValue instanceof LocalDateTime && newValue instanceof LocalDateTime) {
            return !oldValue.equals(newValue);
        }

        // 处理枚举
        if (oldValue instanceof Enum && newValue instanceof Enum) {
            return oldValue != newValue;
        }

        // 默认使用ObjectUtil.notEqual
        return ObjectUtil.notEqual(oldValue, newValue);
    }

    /**
     * 通过反射扫描指定类及其父类中所有标记了@LogField注解的字段
     */
    private static List<Field> findLogFields(Class<?> clazz) {
        if (clazz == null || clazz == Object.class) {
            return Collections.emptyList();
        }

        try {
            List<Field> logFields = new ArrayList<>();

            // 1. 扫描当前类
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(LogField.class)) {
                    try {
                        field.setAccessible(true); // 尝试设置可访问
                        logFields.add(field);
                    } catch (SecurityException e) {
                        // 降级：记录警告但继续
                        log.warn("无法设置字段可访问性: {}.{}",
                                clazz.getSimpleName(), field.getName());
                        // 仍然添加字段，后续通过ReflectUtil访问
                        logFields.add(field);
                    }
                }
            }

            // 2. 递归扫描父类（排除Object.class）
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                List<Field> superFields = findLogFields(superClass);
                logFields.addAll(superFields);
            }

            return logFields.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(logFields);
        } catch (Exception e) {
            log.error("扫描@LogField字段失败: {}", clazz.getName(), e);
            return Collections.emptyList();
        }
    }
}