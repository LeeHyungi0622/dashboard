package io.dtonic.dhubingestmodule.aop.logging;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import io.dtonic.dhubingestmodule.aop.logging.mapper.LoggingMapper;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
@Aspect
public class AccessLogAspect {
    
    @Value("${spring.datasource.logging.use.yn}")
    private String loggingUseYn;

    @Value("${spring.datasource.logging.moduleName}")
    private String moduleName;

    @Autowired
    private LoggingMapper loggingMapper;

    @Pointcut("@annotation(LogAccessRest)")
    public void accesslog(){}
    
    @Around("accesslog()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        if(loggingUseYn.equals("Y")){
            String requestAt = LocalDateTime.now().toString();
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String path = new String();
            for(Annotation annotation : methodSignature.getMethod().getAnnotations()) {
                if(annotation.toString().contains("GetMapping")){
                    GetMapping getMapping = (GetMapping) annotation;
                    path =  getMapping.value()[0];
                }
                else if (annotation.toString().contains("PostMapping")){
                    PostMapping postMapping = (PostMapping) annotation;
                    path =  postMapping.value()[0];
                }
                else if (annotation.toString().contains("PutMapping")){
                    PutMapping putMapping = (PutMapping) annotation;
                    path =  putMapping.value()[0];
                }
                else if(annotation.toString().contains("PatchMapping")){
                    PatchMapping patchMapping = (PatchMapping) annotation;
                    path =  patchMapping.value()[0];
                }
                else if(annotation.toString().contains("DeleteMapping")){
                    DeleteMapping deleteMapping = (DeleteMapping) annotation;
                    path =  deleteMapping.value()[0];
                }
            }
            String httpMethod = new String();
            String accessIp = new String();
            String userId = new String();
            for(Object arg :joinPoint.getArgs()){
                if(arg.getClass().getSimpleName().contains("Request")){
                    HttpServletRequest request = (HttpServletRequest) arg;
                    httpMethod = request.getMethod();
                    accessIp = getPublicIP(request);
                    userId =
                    request.getRemoteUser() == null
                      ? "Unknown"
                      : request.getRemoteUser();
                }
            }
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

    public String getPublicIP(HttpServletRequest req) throws Exception {
        String ip = req.getHeader("X_FORWARDED_FOR");
        if (ip == null) {
          ip = req.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
          ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
          ip = req.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
          ip = req.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
          ip = req.getRemoteAddr();
        }
        return ip;
      }
}
