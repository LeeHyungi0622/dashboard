package io.dtonic.dhubingestmodule.pipeline.mapper;

import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVOtoDB;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PipelineMapper {
    List<String> getDataCollector();

    List<PipelineListResponseVO> getPipelineList();

    List<PropertyVO> getPipelineproperties(@Param("adaptorName") String adaptorName);

    PipelineVO getPipeline(@Param("id") Integer id);

    int changePipelineStatus(@Param("id") Integer id, @Param("status") String status);

    int deletePipeline(
        @Param("id") Integer id,
        @Param("status") String status
        );

    int deletePipelineDrafts(@Param("id") Integer id);

    Boolean isExists(@Param("id") Integer id);


    void createPipeline(
        @Param("pipelineVOtoDB") PipelineVOtoDB pipelineVOtoDB
    );

    int updatePipeline(
        @Param("id") Integer id,
        @Param("name") String name,
        @Param("detail") String detail,
        @Param("dataSet") String dataSet,
        @Param("dataModel") String dataModel,
        @Param("processorGroupId") String processorGroupId,
        @Param("collector") String collector,
        @Param("filter") String filter,
        @Param("converter") String converter
    );

    int updatePipelineProcessgroupId(
        @Param("id") Integer id,
        @Param("processorGroupId") String processorGroupId
    );

}
