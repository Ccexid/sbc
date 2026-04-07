package me.link.bootstrap.core.mybatis.handler;

import cn.hutool.core.util.StrUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 字符串与 Set 类型转换器
 * <p>
 * 用于 MyBatis 框架中 Java {@code Set<String>} 类型与数据库 VARCHAR 类型之间的双向转换。
 * 写入时：将 Set 中的元素以逗号分隔合并为字符串存储；
 * 读取时：将数据库中的逗号分隔字符串解析并去重后转换为 Set。
 * </p>
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(List.class)
public class StringToSetTypeHandler extends BaseTypeHandler<Set<String>> {

    /**
     * 设置非空参数到 PreparedStatement
     * <p>
     * 将 {@code Set<String>} 转换为逗号分隔的字符串存入数据库。
     *
     * @param ps        PreparedStatement 对象
     * @param i         参数索引位置（从1开始）
     * @param parameter 要转换的 {@code Set<String>} 参数，保证不为 null
     * @param jdbcType  JDBC 类型
     * @throws SQLException SQL 异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Set<String> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, String.join(",", parameter));
    }

    /**
     * 从 ResultSet 中根据列名获取数据并转换为 {@code Set<String>}
     *
     * @param rs         ResultSet 结果集对象
     * @param columnName 列名
     * @return 转换后的 {@code Set<String>}，如果值为 null 或空则返回空集合
     * @throws SQLException SQL 异常
     */
    @Override
    public Set<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return stringToSet(rs.getString(columnName));
    }

    /**
     * 从 ResultSet 中根据列索引获取数据并转换为 {@code Set<String>}
     *
     * @param rs          ResultSet 结果集对象
     * @param columnIndex 列索引（从1开始）
     * @return 转换后的 {@code Set<String>}，如果值为 null 或空则返回空集合
     * @throws SQLException SQL 异常
     */
    @Override
    public Set<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return stringToSet(rs.getString(columnIndex));
    }

    /**
     * 从 CallableStatement 中根据列索引获取数据并转换为 {@code Set<String>}
     *
     * @param cs          CallableStatement 存储过程调用对象
     * @param columnIndex 列索引（从1开始）
     * @return 转换后的 {@code Set<String>}，如果值为 null 或空则返回空集合
     * @throws SQLException SQL 异常
     */
    @Override
    public Set<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return stringToSet(cs.getString(columnIndex));
    }

    /**
     * 将数据库中的逗号分隔字符串转换为 {@code Set<String>}
     * <p>
     * 处理逻辑：
     * 1. 若字符串为 null 或空白，返回空集合；
     * 2. 按逗号分割字符串；
     * 3. 去除每个元素的首尾空格；
     * 4. 过滤掉空字符串元素；
     * 5. 收集为 Set 以自动去重。
     *
     * @param columnValue 数据库中的字符串值
     * @return 转换后的 {@code Set<String>}
     */
    private Set<String> stringToSet(String columnValue) {
        if (StrUtil.isBlank(columnValue)) {
            return Collections.emptySet();
        }
        return Arrays.stream(columnValue.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
    }
}
