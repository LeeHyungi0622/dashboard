package io.dtonic.dhubingestmodule.history.aop.command;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.binding.MapperMethod.MethodSignature;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import io.dtonic.dhubingestmodule.common.code.CommandStatusCode;
import io.dtonic.dhubingestmodule.common.code.PipelineStatusCode;
import io.dtonic.dhubingestmodule.history.service.HistorySVC;
import io.dtonic.dhubingestmodule.history.vo.CommandVO;

@Aspect
@Component
public class CommandHistoryAspect {

    @Autowired
    private HistorySVC historySVC;

    @Around("@annotation(io.dtonic.dhubingestmodule.history.aop.command.ActionHistory)")
    public Object saveActionHistory(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        CommandHistory actionHistory = signature.getReturnType().getAnnotation(CommandHistory.class);

        CommandVO commandVO = new CommandVO();
        commandVO.setPipelineId(id);
        commandVO.setCommand(actionHistory.command().getCode());
        if (actionHistory.command().equals(CommandStatusCode.COMMAND_CREATE)){
            commandVO.setStatus(CommandStatusCode.COMMAND_STATUS_CREATING.getCode());
        } else if (actionHistory.command().equals(CommandStatusCode.COMMAND_UPDATE)){
            commandVO.setStatus(CommandStatusCode.COMMAND_STATUS_UPDATING.getCode());
        } else if (actionHistory.command().equals(CommandStatusCode.COMMAND))
        commandVO.setUserId(userId);

        Integer commandId = historySVC.createCommand(commandVO);

        if (processorGroupId != null) {
            //임시 파이프라인 삭제
            pipelineDraftSVC.deletePipelineDrafts(id);
            pipelineMapper.updatePipelineProcessgroupId(pipelineId, processorGroupId);
            historySVC.updateCommand(commandId,CommandStatusCode.COMMAND_STATUS_SUCCEED.getCode());
            return ResponseEntity.ok().build();
        } else {
            historySVC.updateCommand(commandId,CommandStatusCode.COMMAND_STATUS_FAILED.getCode());
            changePipelineStatus(pipelineId, PipelineStatusCode.PIPELINE_STATUS_FAILED.getCode());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nifi error");
        }
        Object proceed = joinPoint.proceed();
            return proceed; 
        
    }
}
