package io.dtonic.dhubingestmodule.pipeline.mapper;

import io.dtonic.dhubingestmodule.pipeline.vo.PipelineCreateVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineResponseVO;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PipelineMapper {
    List<PipelineResponseVO> getPipelineDraftsList(
        @Param("searchObject") String searchObject,
        @Param("searchValue") String searchValue
    );

    PipelineResponseVO getPipeline(@Param("id") Integer id);

    PipelineResponseVO getPipelineDrafts(@Param("id") Integer id);

    void startPipeline(@Param("id") Integer id);

    void stopPipeline(@Param("id") Integer id);

    void deletePipeline(@Param("id") Integer id);

    void deletePipelineDrafts(@Param("id") Integer id);

    Boolean isExists(@Param("id") Integer id);

    Boolean isExistsDrafts(@Param("id") Integer id);
    // void createPipeline(
    //     @Param("creator") String creator,
    //     @Param("name") String name,
    //     @Param("detail") String detail,
    //     @Param("status") String status,
    //     @Param("data_set") String data_set,
    //     @Param("collector") String collector,
    //     @Param("filter") String filter,
    //     @Param("converter") String converter,
    //     @Param("createdAt") Date createdAt,
    //     @Param("modifiedAt") Date modifiedAt
    // );

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
