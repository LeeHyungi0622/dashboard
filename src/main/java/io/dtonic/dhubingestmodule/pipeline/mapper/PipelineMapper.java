package io.dtonic.dhubingestmodule.pipeline.mapper;

import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PipelineMapper {
    List<PipelineListResponseVO> getPipelineList();

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
}
