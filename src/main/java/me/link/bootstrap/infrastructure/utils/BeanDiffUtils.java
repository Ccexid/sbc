package me.link.bootstrap.infrastructure.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.domain.log.model.FieldChangeDetail;
import me.link.bootstrap.infrastructure.annotation.LogField;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean 差异比较工具类 (DDD 基础设施层)
 * 优化点：加入字段缓存、支持脱敏、日期格式化及空值友好处理
 */
@Slf4j
public class BeanDiffUtils {

    // 缓存类的字段信息，避免频繁反射损耗性能
    // 使用 ConcurrentHashMap 保证多线程环境下的线程安全
    private static final Map<Class<?>, Field[]> FIELDS_CACHE = new ConcurrentHashMap<>();

    /**
     * 比较两个对象之间的差异 (DDD 模式优化版)
     *
     * @param oldObj 旧对象
     * @param newObj 新对象
     * @return 包含 FieldChangeDetail 的列表，类型安全且易于序列化
     */
    public static List<FieldChangeDetail> diff(Object oldObj, Object newObj) {
        // 1. 前置检查：空值或类型不匹配直接返回空列表
        if (oldObj == null || newObj == null || !oldObj.getClass().equals(newObj.getClass())) {
            if (oldObj != null && newObj != null) {
                log.warn("BeanDiffUtils: Types mismatch [{} vs {}]", oldObj.getClass(), newObj.getClass());
            }
            return Collections.emptyList();
        }

        // 2. 获取缓存的字段列表
        // 使用 FIELDS_CACHE 避免高频操作下的反射性能损耗
        Field[] fields = FIELDS_CACHE.computeIfAbsent(oldObj.getClass(), ReflectUtil::getFields);
        List<FieldChangeDetail> changeDetails = new ArrayList<>();

        for (Field field : fields) {
            // 3. 仅处理带有 @LogField 注解的字段
            LogField logField = field.getAnnotation(LogField.class);
            if (logField == null) {
                continue;
            }

            try {
                // 4. 获取字段值
                Object oldValue = ReflectUtil.getFieldValue(oldObj, field);
                Object newValue = ReflectUtil.getFieldValue(newObj, field);

                // 5. 核心逻辑：判断值是否发生实质性变化
                if (ObjectUtil.notEqual(oldValue, newValue)) {

                    // 6. 构造领域模型 (FieldChangeDetail)
                    // 使用我们之前定义的 formatValue 方法进行脱敏、日期格式化和字典翻译
                    FieldChangeDetail detail = FieldChangeDetail.builder()
                            .fieldLabel(logField.value())
                            .fieldName(field.getName())
                            .beforeValue(formatValue(oldValue, logField))
                            .afterValue(formatValue(newValue, logField))
                            .build();

                    changeDetails.add(detail);
                }
            } catch (Exception e) {
                // 局部异常隔离，不影响其他字段的对比
                log.error("[BeanDiff] 字段 [{}] 对比失败: {}", field.getName(), e.getMessage());
            }
        }

        return changeDetails;
    }

    /**
     * 根据 @LogField 注解配置格式化输出值
     * 处理逻辑包括：空值处理、脱敏、日期格式化、字典标记
     *
     * @param value 原始值
     * @param ann   @LogField 注解实例
     * @return 格式化后的字符串
     */
    private static String formatValue(Object value, LogField ann) {
        // 步骤 1: 空值处理
        // 如果值为 null，直接返回空字符串，避免后续抛出空指针异常
        if (value == null) {
            return "";
        }

        // 步骤 2: 脱敏处理
        // 如果注解标记为敏感字段 (isSensitive=true)，则返回掩码字符串
        if (ann.isSensitive()) {
            return "******";
        }

        // 步骤 3: 日期格式化
        // 如果注解中指定了日期格式 (dateFormat 不为空)，尝试对日期类型进行格式化
        if (StringUtils.isNotBlank(ann.dateFormat())) {
            if (value instanceof Date) {
                // 处理 java.util.Date 类型
                return DateUtil.format((Date) value, ann.dateFormat());
            } else if (value instanceof LocalDateTime) {
                // 处理 java.time.LocalDateTime 类型
                return DateUtil.format((LocalDateTime) value, ann.dateFormat());
            }
        }

        // 步骤 4: 字典翻译标记
        // 如果注解中指定了字典类型 (dictType 不为空)，在此处添加标记前缀
        // 注意：实际的字典文本翻译通常需要在 Application 层注入 DictService 完成，此处仅作标识
        if (StringUtils.isNotBlank(ann.dictType())) {
            // return dictService.getLabel(ann.dictType(), value.toString()); // 如需真实翻译可取消注释并注入服务
            return String.format("[%s]%s", ann.dictType(), value);
        }

        // 步骤 5: 默认处理
        // 如果以上条件都不满足，直接调用 toString() 返回字符串形式
        return String.valueOf(value);
    }
}