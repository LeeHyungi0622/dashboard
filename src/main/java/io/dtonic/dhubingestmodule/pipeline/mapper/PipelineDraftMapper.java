package io.dtonic.dhubingestmodule.pipeline.mapper;

import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.vo.DataCollectorVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PipelineDraftMapper {
    List<String> getDataCollector();

    List<PropertyVO> getPipelineproperties(@Param("adaptorName") String adaptorName);

    List<PipelineVO> getPipelineDraftsList();

    PipelineVO getPipelineDrafts(@Param("id") Integer id);

    int deletePipelineDrafts(@Param("id") Integer id);

    Boolean isExistsDrafts(@Param("id") Integer id);

    int createPipelineDrafts(
        @Param("name") String name,
        @Param("creator") String creator,
        @Param("detail") String detail
    );
    int updatePipelineDrafts(
        @Param("id") Integer id,
        @Param("name") String name,
        @Param("detail") String detail,
        @Param("dataSet") String dataSet,
        @Param("flowJsonString") String flowJsonString,
        @Param("nifiFlowType") String nifiFlowType
    );
    NiFiComponentVO getAdaptorinfo(@Param("adaptorId") Integer adaptorId);

    Integer getPipelineIdByname(@Param("name") String name);
    Boolean isExistsNameDrafts(@Param("name") String name);
}
