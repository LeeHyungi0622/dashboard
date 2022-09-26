package io.dtonic.dhubingestmodule.pipeline.mapper;

import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.vo.DataCollectorVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PipelineDraftMapper {
    List<DataCollectorVO> getDataCollector();

    List<PropertyVO> getPipelineproperties(@Param("adaptorid") Integer adaptorid);

    List<PipelineVO> getPipelineDraftsList(
        @Param("searchObject") String searchObject,
        @Param("searchValue") String searchValue
    );

    PipelineVO getPipelineDrafts(@Param("id") Integer id);

    void deletePipelineDrafts(@Param("id") Integer id);

    Boolean isExistsDrafts(@Param("id") Integer id);

    void createPipelineDrafts(
        @Param("name") String name,
        @Param("creator") String creator,
        @Param("detail") String detail
    );
    void updatePipelineDrafts(
        @Param("id") Integer id,
        @Param("name") String name,
        @Param("detail") String detail,
        @Param("dataSet") String dataSet,
        @Param("flowJsonString") String flowJsonString,
        @Param("nifiFlowType") String nifiFlowType
    );
}
