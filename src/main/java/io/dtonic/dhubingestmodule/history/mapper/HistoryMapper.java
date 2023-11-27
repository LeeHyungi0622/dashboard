package io.dtonic.dhubingestmodule.history.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import io.dtonic.dhubingestmodule.history.vo.CommandVO;
import io.dtonic.dhubingestmodule.history.vo.TaskVO;
@Mapper
public interface HistoryMapper {
    
    void createCommand(
        @Param("commandVO") CommandVO commandVO
    );

    int updateCommand(
        @Param("id") Integer id,
        @Param("status") String status
    );
    int updateCommandPipelineId(
        @Param("id") Integer id,
        @Param("pipelineId") Integer pipelineId
    );

    void createTask(
        @Param("taskVO") TaskVO taskVO
    );

    int updateTask(
        @Param("id") Integer id,
        @Param("status") String status
    );
    
    List<CommandVO> getPipelineCmdHistory(
        @Param("pipelineId") Integer pipelineId
    );
    List<TaskVO> getPipelineTaskHistory(
        @Param("commandId") Integer commandId
    );
}
