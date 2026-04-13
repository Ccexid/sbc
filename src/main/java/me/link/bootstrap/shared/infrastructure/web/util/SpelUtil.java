package me.link.bootstrap.shared.infrastructure.web.util;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance and robust SpEL expression parsing utility.
 * Features concurrent caching with bounded size to prevent memory leaks.
 */
@Slf4j
public final class SpelUtil {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    /**
     * 线程安全的SpEL表达式缓存映射表。
     * 使用ConcurrentHashMap保证computeIfAbsent操作的原子性，避免并发解析开销。
     * 初始容量设置为128以减少扩容次数。
     */
    private static final Map<String, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>(128);

    /**
     * 缓存最大容量阈值。
     * 当缓存大小超过此值时会触发清理操作，防止内存泄漏。
     */
    private static final int MAX_CACHE_SIZE = 512;

    private SpelUtil() {
        // Utility class
    }

    /**
     * 在AOP切面上下文中评估SpEL表达式并返回字符串结果。
     * <p>
     * 该方法支持以下功能：
     * <ul>
     *   <li>表达式缓存以提升性能</li>
     *   <li>AOP方法参数自动绑定（如 #paramName）</li>
     *   <li>额外变量注入（如 #result）</li>
     *   <li>优雅降级：解析失败时返回原始表达式字符串</li>
     * </ul>
     *
     * @param joinPoint        AOP连接点，提供方法签名、参数和目标对象信息
     * @param expressionString SpEL表达式字符串，例如 "#user.name" 或 "#result.code"
     * @param extraVariables   额外变量映射表，键为变量名（不含#），值为变量值；可为null
     * @return 表达式评估结果的字符串表示；如果结果为null则返回空字符串；如果解析失败则返回原始表达式字符串
     */
    public static String parseExpression(
            ProceedingJoinPoint joinPoint,
            String expressionString,
            Map<String, Object> extraVariables) {

        // 输入验证：处理null、空字符串和纯空白字符串
        if (expressionString == null || expressionString.isBlank()) {
            return "";
        }

        // 快速路径：不包含SpEL变量标识符，直接返回原始字符串
        if (!expressionString.contains("#")) {
            return expressionString;
        }

        try {
            // 线程安全地获取或解析表达式
            Expression expression = getCachedExpression(expressionString);

            EvaluationContext context = createEvaluationContext(joinPoint, extraVariables);

            Object value = expression.getValue(context);
            return value != null ? value.toString() : "";

        } catch (SpelParseException | SpelEvaluationException e) {
            // 记录详细的诊断信息用于生产环境问题排查
            log.warn("SpEL Evaluation Failed | Expression: [{}] | Method: [{}] | Target: [{}] | Message: {}",
                    expressionString,
                    joinPoint.getSignature().toShortString(),
                    joinPoint.getTarget().getClass().getSimpleName(),
                    e.getMessage());
            return expressionString;
        }
    }

    /**
     * 从缓存中获取已解析的SpEL表达式，如果不存在则解析并缓存。
     * <p>
     * 使用ConcurrentHashMap的computeIfAbsent方法保证解析操作的原子性，
     * 避免多线程环境下的重复解析。当缓存大小超过MAX_CACHE_SIZE阈值时，
     * 会清空整个缓存以释放内存空间。
     *
     * @param expressionString 待获取或解析的SpEL表达式字符串
     * @return 已解析的Expression对象
     */
    private static Expression getCachedExpression(String expressionString) {
        // ConcurrentHashMap.computeIfAbsent在现代JDK中是原子操作
        Expression expr = EXPRESSION_CACHE.computeIfAbsent(
                expressionString, EXPRESSION_PARSER::parseExpression);

        // 简单有效的缓存驱逐策略：超过阈值时清空缓存
        if (EXPRESSION_CACHE.size() > MAX_CACHE_SIZE) {
            log.debug("SpEL cache threshold exceeded, clearing cache to release memory.");
            EXPRESSION_CACHE.clear();
        }
        return expr;
    }

    /**
     * 为AOP参数绑定创建基于方法的SpEL评估上下文。
     * <p>
     * 该方法利用MethodBasedEvaluationContext实现方法参数的懒加载发现机制，
     * 支持通过参数名称（如 #userId）访问方法参数。同时支持注入额外变量。
     *
     * @param joinPoint      AOP连接点，提供方法签名、参数数组和目标对象
     * @param extraVariables 需要注入到上下文中的额外变量映射表；可为null
     * @return 配置好的MethodBasedEvaluationContext实例
     */
    private static EvaluationContext createEvaluationContext(
            ProceedingJoinPoint joinPoint,
            Map<String, Object> extraVariables) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object target = joinPoint.getTarget();
        Object[] args = joinPoint.getArgs();

        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                target, method, args, PARAMETER_NAME_DISCOVERER);

        // 空安全检查：确保跨Spring版本的兼容性
        if (extraVariables != null) {
            context.setVariables(extraVariables);
        }

        return context;
    }
}