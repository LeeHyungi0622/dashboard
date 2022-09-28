package io.dtonic.dhubingestmodule.pipeline.mapper;

import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.vo.DataCollectorVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PipelineMapper {
    List<DataCollectorVO> getDataCollector();

    List<PipelineListResponseVO> getPipelineList();

    List<PropertyVO> getPipelineproperties(@Param("adaptorName") String adaptorName);

    PipelineVO getPipeline(@Param("id") Integer id);

    int changePipelineStatus(@Param("id") Integer id, @Param("status") String status);

    int deletePipeline(@Param("id") Integer id);

    int deletePipelineDrafts(@Param("id") Integer id);

    Boolean isExists(@Param("id") Integer id);

    int createPipeline(
        @Param("creator") String creator,
        @Param("name") String name,
        @Param("detail") String detail,
        @Param("status") String status,
        @Param("dataSet") String dataSet,
        @Param("collector") String collector,
        @Param("filter") String filter,
        @Param("converter") String converter
    );

    void updatePipeline(
        @Param("id") Integer id,
        @Param("name") String name,
        @Param("detail") String detail,
        @Param("dataSet") String dataSet,
        @Param("flowJsonString") String flowJsonString,
        @Param("nifiFlowType") String nifiFlowType
    );
}
