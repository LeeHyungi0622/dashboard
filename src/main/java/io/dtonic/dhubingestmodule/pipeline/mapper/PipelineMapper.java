package io.dtonic.dhubingestmodule.pipeline.mapper;

import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PipelineMapper {
    PipelineVO getPipeline(@Param("id") String id);
}
