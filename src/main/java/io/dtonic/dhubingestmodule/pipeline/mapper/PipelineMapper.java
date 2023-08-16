package io.dtonic.dhubingestmodule.pipeline.mapper;

import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PipelineMapper {
    List<String> getDataCollector();

    List<PipelineListResponseVO> getPipelineList();

    List<PropertyVO> getPipelineproperties(@Param("adaptorName") String adaptorName);
    /**
     * [Mapper Interface Get Pipeline Detail by id]
     * @param id Pipeline id
     * @return PipelineVO
     * 
     * @since 2023. 8. 16
     * @version 1.2.0
     * @auther Justin
     */
    PipelineVO getPipeline(@Param("id") Integer id);

    int changePipelineStatus(@Param("id") Integer id, @Param("status") String status);

    int deletePipeline(
        @Param("id") Integer id,
        @Param("status") String status
        );

    int deletePipelineDrafts(@Param("id") Integer id);

    Boolean isExists(@Param("id") Integer id);


    void createPipeline(
        @Param("pipelineVO") PipelineVO pipelineVO
    );

    int updatePipeline(
        @Param("pipelineVO") PipelineVO pipelineVO
    );

    int updatePipelineProcessgroupId(
        @Param("id") Integer id,
        @Param("processorGroupId") String processorGroupId
    );

}
