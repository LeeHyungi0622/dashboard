package io.dtonic.dhubingestmodule.pipeline.mapper;

import io.dtonic.dhubingestmodule.pipeline.vo.PipelineResponseVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PipelineDraftMapper {
    List<PipelineResponseVO> getPipelineDraftsList(
        @Param("searchObject") String searchObject,
        @Param("searchValue") String searchValue
    );

    PipelineResponseVO getPipelineDrafts(@Param("id") Integer id);

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
        @Param("flowJsonString") String flowJsonString,
        @Param("nifiFlowType") String nifiFlowType
    );
}
