package com.ziheng.ai.infrastructure.vector.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * JSONB 类型处理器
 */
public class JsonbTypeHandler extends BaseTypeHandler<Map<String, Object>> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 泛型类型引用
     */
    private static final TypeReference<Map<String, Object>> TYPE_REFERENCE = new TypeReference<>() {};

    /**
     * 设置非空参数
     *
     * @param ps       PreparedStatement
     * @param i        参数索引
     * @param parameter 参数值
     * @param jdbcType  Jdbc类型
     * @throws SQLException SQL异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("jsonb");
        try {
            pgObject.setValue(OBJECT_MAPPER.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("JSONB 数据序列化失败", e);
        }
        ps.setObject(i, pgObject);
    }

    /**
     * 从结果集中获取非空结果
     *
     * @param rs      ResultSet
     * @param columnName 列名
     * @return 非空结果
     * @throws SQLException SQL异常
     */
    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    /**
     * 从结果集中获取非空结果
     *
     * @param rs      ResultSet
     * @param columnIndex 列索引
     * @return 非空结果
     * @throws SQLException SQL异常
     */
    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    /**
     * 从存储过程中获取非空结果
     *
     * @param cs      CallableStatement
     * @param columnIndex 列索引
     * @return 非空结果
     * @throws SQLException SQL异常
     */
    @Override
    public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }

    /**
     * 解析 JSON 数据
     *
     * @param json JSON 字符串
     * @return JSON 数据
     * @throws SQLException SQL异常
     */
    private Map<String, Object> parseJson(String json) throws SQLException {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, TYPE_REFERENCE);
        } catch (JsonProcessingException e) {
            throw new SQLException("JSONB 数据反序列化失败", e);
        }
    }

}
