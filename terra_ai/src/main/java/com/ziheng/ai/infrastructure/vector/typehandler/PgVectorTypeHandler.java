package com.ziheng.ai.infrastructure.vector.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PG向量类型处理器
 */
public class PgVectorTypeHandler  extends BaseTypeHandler<float[]> {
    /**
     * 设置非空参数
     * @param ps       PreparedStatement
     * @param i        参数索引
     * @param parameter 参数值
     * @param jdbcType  Jdbc类型
     * @throws SQLException SQL异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType) throws SQLException {
        // 将 float[] 转换成 pgvector 字面量，例如 [0.1,0.2,0.3]
        StringBuilder vector = new StringBuilder("[");
        for (int index = 0; index < parameter.length; index++) {
            if (index > 0) {
                vector.append(",");
            }
            vector.append(parameter[index]);
        }
        vector.append("]");

        PGobject pgObject = new PGobject();
        pgObject.setType("vector");
        pgObject.setValue(vector.toString());

        ps.setObject(i, pgObject);
    }

    /**
     * 从结果集中获取值，使用列名
     * @param rs       结果集
     * @param columnName 列名
     * @return 值
     * @throws SQLException SQL异常
     */
    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseVector(rs.getString(columnName));
    }

    /**
     * 从结果集中获取值，使用列索引
     * @param rs       结果集
     * @param columnIndex 列索引
     * @return 值
     * @throws SQLException SQL异常
     */
    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseVector(rs.getString(columnIndex));
    }

    /**
     * 从存储过程中获取值，使用列索引
     * @param cs       存储过程调用结果集
     * @param columnIndex 列索引
     * @return 值
     * @throws SQLException SQL异常
     */
    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseVector(cs.getString(columnIndex));
    }

    /**
     * 解析向量值
     * @param value 值
     * @return 向量
     */
    private float[] parseVector(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        // pgvector 返回格式：[0.1,0.2,0.3]
        String content = value.substring(1, value.length() - 1);
        if (content.isBlank()) {
            return new float[0];
        }
        String[] values = content.split(",");
        float[] result = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = Float.parseFloat(values[i].trim());
        }
        return result;
    }
}
