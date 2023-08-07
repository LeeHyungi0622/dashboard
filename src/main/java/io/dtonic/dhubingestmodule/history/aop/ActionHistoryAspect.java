package io.dtonic.dhubingestmodule.history.aop;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import io.dtonic.dhubingestmodule.common.code.CommandStatusCode;
import io.dtonic.dhubingestmodule.history.vo.CommandVO;

@Aspect
@Component
public class ActionHistoryAspect {

    @Pointcut("@annotation(ActionHistory)")
    public void actionHistory(){}

    @Around("actionHistory()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        joinPoint.getArgs()[0].getClass().getDeclaredFields();
        CommandVO commandVO = new CommandVO();
        commandVO.setPipelineId(id);
        commandVO.setCommand(CommandStatusCode.COMMAND_UPDATE.getCode());
        commandVO.setStatus(CommandStatusCode.COMMAND_STATUS_UPDATING.getCode());
        commandVO.setUserId(userId);

        Integer commandId = createCommand(commandVO);

            Object proceed = joinPoint.proceed();
            String responseAt = LocalDateTime.now().toString();
            if(proceed.getClass().equals(ResponseEntity.class)){
                ResponseEntity response = (ResponseEntity) proceed;
                String httpStatus = Integer.toString(response.getStatusCodeValue());
                String body = null;
                if(httpStatus.equals("400") || httpStatus.equals("401") || httpStatus.equals("404") || httpStatus.equals("415") || httpStatus.equals("500")){
                    body = response.getBody().toString();
                }
                loggingMapper.createloggingDetail(requestAt, responseAt, moduleName, path, accessIp, userId, httpStatus, body, httpMethod);
            }
            else{
                loggingMapper.createloggingDetail(requestAt, responseAt, moduleName, path, accessIp, userId, "200",null, httpMethod);
            }
            return proceed; 
        }
        else{
            Object proceed = joinPoint.proceed();
            return proceed; 
        }
        
    }
}
