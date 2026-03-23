package me.link.bootstrap.core.exception.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.core.exception.BusinessException;
import me.link.bootstrap.core.exception.IErrorCode;

/**
 * BusinessException 工具类
 * 核心职责：提供高性能的 {} 占位符格式化，并快速构建业务异常对象
 */
@Slf4j
public class BusinessExceptionUtil {

    /**
     * 构建无参数的业务异常
     */
    public static BusinessException exception(IErrorCode errorCode) {
        return new BusinessException(errorCode.getCode(), errorCode.getMsg());
    }

    /**
     * 构建带参数格式化的业务异常
     * 示例：ErrorCode 为 "用户[{}]不存在"，调用 exception(code, "张三") -> "用户[张三]不存在"
     */
    public static BusinessException exception(IErrorCode errorCode, Object... params) {
        String message = doFormat(errorCode.getMsg(), params);
        return new BusinessException(errorCode.getCode(), message);
    }

    /**
     * 高性能格式化方法 (直接复用 yudao 的算法精髓)
     * 使用 {} 作为占位符，比 String.format 更健壮
     */
    public static String doFormat(String pattern, Object... params) {
        if (StrUtil.isEmpty(pattern) || params == null || params.length == 0) {
            return pattern;
        }

        int patternLength = pattern.length();
        StringBuilder sbuf = new StringBuilder(patternLength + 50);
        int i = 0; // 当前处理到的 pattern 索引
        int j;     // 找到的 {} 索引

        for (Object param : params) {
            j = pattern.indexOf("{}", i);
            if (j == -1) {
                log.warn("[doFormat] 参数过多: pattern={}, params={}", pattern, params);
                break;
            }
            sbuf.append(pattern, i, j);
            sbuf.append(param);
            i = j + 2;
        }

        // 拼接剩余部分
        sbuf.append(pattern.substring(i));

        // 校验参数是否过少
        if (pattern.indexOf("{}", i) != -1) {
            log.warn("[doFormat] 参数过少: pattern={}, params={}", pattern, params);
        }

        return sbuf.toString();
    }
}