package me.link.bootstrap.shared.kernel.annotation;

import me.link.bootstrap.shared.infrastructure.mybatis.handler.DefaultDBFieldHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标识需要自动生成业务流水号的字段。
 * <p>
 * 该注解通常配合反射或 AOP 机制使用。在实体类字段上标记后，
 * 系统将在运行时根据配置策略（如前缀、日期格式、数字补零等）自动生成唯一的业务编号。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * public class Order {
 *     // 生成格式为 "ORD" + 日期 (yyyyMMdd) + 6 位数字，例如：ORD20231027000001
 *     @IdGenerator(prefix = "ORD", digit = 6, daily = true)
 *     private String orderNo;
 * }
 * }</pre>
 *
 * @author link
 * @date 2020/5/12 11:04
 * @see DefaultDBFieldHandler
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdGenerator {

    /**
     * 指定生成的流水号前缀。
     * <p>
     * 可用于区分不同的业务类型，例如订单使用 "ORD"，支付使用 "PAY"。
     * 若未设置（保持默认空字符串），则生成的流水号不包含前缀。
     * </p>
     *
     * @return 流水号前缀，默认为空字符串
     */
    String prefix() default "";

    /**
     * 指定流水号中数字部分的固定长度（不足时左侧自动补零）。
     * <p>
     * 例如：设置为 {@code 6} 时，数字 {@code 1} 将显示为 {@code "000001"}。
     * 若设置为 {@code 0} 或负数，则不进行补零操作，直接使用原始数字。
     * </p>
     *
     * @return 数字部分的固定长度，默认为 {@code 0}（不补零）
     */
    int digit() default 0;

    /**
     * 指定是否将日期纳入流水号生成策略。
     * <p>
     * 当值为 {@code true} 时，生成的流水号将包含当前日期（格式通常为 {@code yyyyMMdd}），
     * 且每日的序号计数器会重置；
     * 当值为 {@code false} 时，流水号将全局连续递增，不包含日期信息。
     * </p>
     *
     * @return {@code true} 表示按天生成并重置序号，{@code false} 表示全局连续生成，默认为 {@code true}
     */
    boolean daily() default true;
}
