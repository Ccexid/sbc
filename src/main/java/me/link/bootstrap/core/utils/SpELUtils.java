package me.link.bootstrap.core.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 高性能 SpEL 表达式解析工具类 (带 Expression 缓存)
 */
public class SpelUtils {

    /**
     * 表达式解析器实例 (线程安全)
     */
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 参数名发现器 (用于获取方法参数名称)
     */
    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    /**
     * Expression 缓存：Key 为表达式字符串，Value 为编译后的 Expression 对象
     */
    private static final Map<String, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>(128);

    /**
     * 解析表达式并返回字符串结果
     *
     * @param joinPoint 切点信息
     * @param spel      待解析的表达式
     * @param variables 额外注入的变量 (如 #result)
     * @return 解析后的字符串结果
     */
    public static String parse(ProceedingJoinPoint joinPoint, String spel, Map<String, Object> variables) {
        if (spel == null || spel.isBlank()) {
            return "";
        }

        try {
            // 1. 获取或创建缓存的 Expression 对象
            Expression expression = EXPRESSION_CACHE.computeIfAbsent(spel, PARSER::parseExpression);

            // 2. 构建 EvaluationContext (可以进一步优化 Context 的重复创建，但在 AOP 中参数是动态的)
            EvaluationContext context = getContext(joinPoint, variables);

            // 3. 执行解析
            Object value = expression.getValue(context);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            // 记录异常并降级：返回原始 SpEL 字符串，确保业务不中断
            return spel;
        }
    }

    /**
     * 构建解析上下文并绑定参数变量
     */
    private static EvaluationContext getContext(ProceedingJoinPoint joinPoint, Map<String, Object> variables) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 获取参数值
        Object[] args = joinPoint.getArgs();
        // 获取参数名
        String[] paramNames = NAME_DISCOVERER.getParameterNames(method);

        // 绑定参数名与参数值
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        // 绑定额外的变量 (如 #result)
        if (variables != null && !variables.isEmpty()) {
            variables.forEach(context::setVariable);
        }

        return context;
    }
}