package io.dtonic.dhubingestmodule.pipeline.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import io.dtonic.dhubingestmodule.pipeline.vo.CommandVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVOtoDB;

@Mapper
public interface PipelineMapperTest {
    void createCommandtest(
        @Param("commandVO") CommandVO commandVO
    );
    
    // int createCommandtest(
    //     @Param("pipelineId") Integer pipelineId,
    //     @Param("command") String command,
    //     @Param("status") String status,
    //     @Param("userId") String userId
    // );

    void createPipelinetest(
//        @Param("pipelineVO") PipelineVO pipelineVO
        @Param("pipelineVO") PipelineVOtoDB pipelineVO
    );
}
