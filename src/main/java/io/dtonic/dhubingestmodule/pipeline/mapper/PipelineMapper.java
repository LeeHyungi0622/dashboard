package io.dtonic.dhubingestmodule.pipeline.mapper;

import io.dtonic.dhubingestmodule.pipeline.vo.PipelineCreateVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineResponseVO;
import java.util.Date;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PipelineMapper {
    PipelineResponseVO getPipeline(@Param("id") Integer id);

    PipelineResponseVO getPipelineDraftsList();

    PipelineResponseVO getPipelineDraftsById(@Param("id") Integer id);

    void startPipeline(@Param("id") Integer id);

    void stopPipeline(@Param("id") Integer id);

    void deletePipeline(@Param("id") Integer id);

    Boolean isExists(@Param("id") Integer id);
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
}
