package io.dtonic.dhubingestmodule.common.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

@Slf4j
public class JsonListTypeHandler<T extends Object> extends BaseTypeHandler<List<T>> {

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(
        PreparedStatement preparedStatement,
        int i,
        List<T> exampleJsons,
        JdbcType jdbcType
    )
        throws SQLException {
        preparedStatement.setString(i, new Gson().toJson(exampleJsons));
    }

    @Override
    public List<T> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return convertToList(resultSet.getString(s));
    }

    @Override
    public List<T> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return convertToList(resultSet.getString(i));
    }

    @Override
    public List<T> getNullableResult(CallableStatement callableStatement, int i)
        throws SQLException {
        return convertToList(callableStatement.getString(i));
    }

    private List<T> convertToList(String s) {
        if (StringUtils.isEmpty(s)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(s, new TypeReference<List<T>>() {});
        } catch (IOException e) {
            log.error("[ExampleJsonTypeHandler] failed to convert string to list");
        }
        return Collections.emptyList();
    }
}
