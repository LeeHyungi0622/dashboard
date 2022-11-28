package io.dtonic.dhubingestmodule.aop.logging.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoggingMapper {
    int createloggingDetail(
        @Param("request_at") String requrestAt,
        @Param("response_at") String responseAt,
        @Param("module_name") String moduleName,
        @Param("menu_uri") String menuUri,
        @Param("access_ip") String accessIp,
        @Param("user_id") String userId,
        @Param("http_status") String httpStatus,
        @Param("error_detail") String errorDetail,
        @Param("http_method") String httpMethod
    );
}
