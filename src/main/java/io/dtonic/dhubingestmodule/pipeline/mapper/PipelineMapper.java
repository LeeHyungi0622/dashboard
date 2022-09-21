package io.dtonic.dhubingestmodule.pipeline.mapper;

import io.dtonic.dhubingestmodule.pipeline.vo.PipelineCreateVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineResponseVO;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PipelineMapper {
    List<PipelineListResponseVO> getPipelineList(
        @Param("searchObject") String searchObject,
        @Param("searchValue") String searchValue,
        @Param("status") String status
    );

    PipelineResponseVO getPipeline(@Param("id") Integer id);

    void changePipelineStatus(@Param("id") Integer id, @Param("status") String status);

    void deletePipeline(@Param("id") Integer id);

    void deletePipelineDrafts(@Param("id") Integer id);

    Boolean isExists(@Param("id") Integer id);

    void createPipeline(
        @Param("creator") String creator,
        @Param("name") String name,
        @Param("detail") String detail,
        @Param("status") String status,
        @Param("data_set") String data_set,
        @Param("collector") String collector,
        @Param("filter") String filter,
        @Param("converter") String converter,
        @Param("createdAt") Date createdAt,
        @Param("modifiedAt") Date modifiedAt
    );
}
