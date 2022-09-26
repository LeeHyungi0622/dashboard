package io.dtonic.dhubingestmodule.common.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class ListTypeHandler implements TypeHandler<List<String>> {

    @Override
    public void setParameter(
        PreparedStatement ps,
        int i,
        List<String> parameter,
        JdbcType jdbcType
    )
        throws SQLException {
        if (parameter != null) {
            ps.setString(i, parameter.toString());
        }
    }

    @Override
    public List<String> getResult(ResultSet rs, String columnName) throws SQLException {
        String columnValueStr = rs.getString(columnName);
        if (columnValueStr != null) {
            return Arrays.asList(columnValueStr.split(","));
        }
        return null;
    }

    @Override
    public List<String> getResult(ResultSet rs, int columnIndex) throws SQLException {
        String columnValueStr = rs.getString(columnIndex);
        if (columnValueStr != null) {
            return Arrays.asList(columnValueStr.split(","));
        }
        return null;
    }

    @Override
    public List<String> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String columnValueStr = cs.getString(columnIndex);
        if (columnValueStr != null) {
            return Arrays.asList(columnValueStr.split(","));
        }
        return null;
    }
}
