package io.dtonic.dhubingestmodule.history.aop.task;

import org.apache.ibatis.binding.MapperMethod.MethodSignature;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import io.dtonic.dhubingestmodule.common.code.TaskStatusCode;
import io.dtonic.dhubingestmodule.history.service.HistorySVC;
import io.dtonic.dhubingestmodule.history.vo.TaskVO;

@Aspect
@Component
public class TaskHistoryAspect {
    @Autowired
    private HistorySVC historySVC;

    @Around("@annotation(io.dtonic.dhubingestmodule.history.aop.command.TaskHistory)")
    public Object saveTaskHistory(ProceedingJoinPoint joinPoint) throws Throwable {
        /* Get Method Args */
        Object[] args = joinPoint.getArgs(); 
        /* 
         * Must be args order:
         * 1. Parent Command Id
         * 2. etc...
         */
        Integer commandId = Integer.parseInt(args[0].toString());
        Integer taskId = null;
        if (commandId != null) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            TaskHistory taskHistory = signature.getReturnType().getAnnotation(TaskHistory.class);
            TaskVO taskVO = new TaskVO();
            taskVO.setCommandId(commandId);
            taskVO.setStatus(TaskStatusCode.TASK_STATUS_WORKING.getCode());
            taskVO.setTaskName(taskHistory.taskName().getCode());
            taskId = historySVC.createTask(taskVO);
            
        }         
        Object proceed = joinPoint.proceed();
        if (taskId != null){
            if (proceed == null){
                /* Update Command History */
                historySVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FAILED.getCode());
            } else {
                /* Update Command History */
                historySVC.updateTask(taskId, TaskStatusCode.TASK_STATUS_FINISH.getCode());
            }
        }
        return proceed; 
        
    }
}
