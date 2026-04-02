package me.link.bootstrap.core.web.servlet;

import cn.hutool.http.HtmlUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * XSS 防护请求包装器
 * <p>
 * 继承 HttpServletRequestWrapper，对 HTTP 请求的头部和参数进行 XSS 过滤处理。
 * 通过重写 getHeader、getParameter 和 getParameterValues 方法，
 * 使用 Hutool 的 HtmlUtil.filter() 对所有输入进行 HTML 过滤，防止 XSS 攻击。
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    /**
     * 构造方法
     *
     * @param request 原始 HTTP 请求对象
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     * 获取请求头并进行 XSS 过滤
     *
     * @param name 请求头名称
     * @return 经过 HTML 过滤后的请求头值
     */
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        /*
         * 使用 Hutool 的 HTML 过滤器对请求头值进行过滤
         * 移除或转义潜在的恶意 HTML 标签和脚本
         */
        return HtmlUtil.filter(value);
    }

    /**
     * 获取请求参数并进行 XSS 过滤
     *
     * @param name 参数名称
     * @return 经过 HTML 过滤后的参数值
     */
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return HtmlUtil.filter(value);
    }

    /**
     * 获取请求参数数组并进行 XSS 过滤
     *
     * @param name 参数名称
     * @return 经过 HTML 过滤后的参数值数组，如果参数不存在则返回 null
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        int count = values.length;
        String[] encodedValues = new String[count];
        /*
         * 遍历参数值数组，对每个值进行 HTML 过滤处理
         * 确保多值参数的所有元素都经过 XSS 防护
         */
        for (int i = 0; i < count; i++) {
            encodedValues[i] = HtmlUtil.filter(values[i]);
        }
        return encodedValues;
    }
}
