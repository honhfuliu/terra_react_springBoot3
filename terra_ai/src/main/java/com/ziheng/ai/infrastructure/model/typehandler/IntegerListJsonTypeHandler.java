package com.ziheng.ai.infrastructure.model.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * MySQL JSON 与 List<Integer> 类型转换
 */
public class IntegerListJsonTypeHandler extends BaseTypeHandler<List<Integer>> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final TypeReference<List<Integer>> TYPE_REFERENCE = new TypeReference<>() {};

    /**
     * 将 List<Integer> 转换为 JSON 写入数据库
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Integer> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, OBJECT_MAPPER.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("Embedding维度列表JSON序列化失败", e);
        }
    }

    /**
     * 根据列名读取
     */
    @Override
    public List<Integer> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    /**
     * 根据列索引读取
     */
    @Override
    public List<Integer> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    /**
     * 从存储过程结果读取
     */
    @Override
    public List<Integer> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }

    /**
     * JSON 转换为 List<Integer>
     */
    private List<Integer> parseJson(String json) throws SQLException {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, TYPE_REFERENCE);
        } catch (Exception e) {
            throw new SQLException("Embedding维度列表JSON反序列化失败", e);
        }
    }
}

