package io.dtonic.dhubingestmodule.pipeline.mapper;

import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

public interface PipelineMapper {
    PipelineVO getPipelineDraftsList();
    PipelineVO getPipelineDraftsById(@Param("id") String id);
}
