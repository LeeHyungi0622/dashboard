package io.dtonic.dhubingestmodule.history.aop.command;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


import io.dtonic.dhubingestmodule.common.code.CommandStatusCode;
import io.dtonic.dhubingestmodule.history.service.HistorySVC;
import io.dtonic.dhubingestmodule.history.vo.CommandVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
@Aspect
@Order(value = 1)
@Component
public class CommandHistoryAspect {

    @Autowired
    private HistorySVC historySVC;

    @Pointcut("@annotation(CommandHistory)")
    public void commandHistoryPointcut() {
    }

    @Around("commandHistoryPointcut()")
    public Object saveCommandHistory(ProceedingJoinPoint joinPoint) throws Throwable {
        /* Get Method Args */
        Object[] args = joinPoint.getArgs(); 
        /* 
         * Must be args order:
         * 1. Pipeline Id
         * 2. User Id
         * 3. Command Id
         * 4. Command Args... etc
         */

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        CommandHistory commandHistory = signature.getMethod().getAnnotation(CommandHistory.class);

        CommandVO commandVO = new CommandVO();
        commandVO.setPipelineId((Integer) args[0]);
        commandVO.setUserId((String) args[1]);
        commandVO.setCommand(commandHistory.command().getCode());
        if (commandHistory.command().equals(CommandStatusCode.COMMAND_CREATE)){
            commandVO.setStatus(CommandStatusCode.COMMAND_STATUS_CREATING.getCode());
        } else if (commandHistory.command().equals(CommandStatusCode.COMMAND_UPDATE)){
            commandVO.setStatus(CommandStatusCode.COMMAND_STATUS_UPDATING.getCode());
        } else if (commandHistory.command().equals(CommandStatusCode.COMMAND_DELETE)){
            commandVO.setStatus(CommandStatusCode.COMMAND_STATUS_DELETING.getCode());
        } else if (commandHistory.command().equals(CommandStatusCode.COMMAND_RUN)){
            commandVO.setStatus(CommandStatusCode.COMMAND_STATUS_RUNNING.getCode());
        } else if (commandHistory.command().equals(CommandStatusCode.COMMAND_STOP)){
            commandVO.setStatus(CommandStatusCode.COMMAND_STATUS_STOPPING.getCode());
        }
        /* Create Command History */
        Integer commandId = historySVC.createCommand(commandVO);
        /* Set Command Id in args */
        args[2] = commandId;
        /* Execute Method */
        Object proceed = joinPoint.proceed(args);
        if (proceed == null){
            /* Update Command History */
            historySVC.updateCommand(commandId, CommandStatusCode.COMMAND_STATUS_FAILED.getCode());
        } else {
            /* Update Pipeline ID when Create Pipeline */
            PipelineVO pipelineVO = (PipelineVO) proceed;
            if (commandHistory.command().equals(CommandStatusCode.COMMAND_CREATE)){
                historySVC.updateCommandPipelineId(commandId, pipelineVO.getId());
            } 
            /* Update Command History */
            historySVC.updateCommand(commandId, CommandStatusCode.COMMAND_STATUS_SUCCEED.getCode());
        }
        return proceed;
    }
}
