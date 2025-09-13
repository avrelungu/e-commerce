package com.example.shared_common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@ConditionalOnProperty(name = "logging.aspect.enabled", havingValue = "true", matchIfMissing = false)
public class LoggingAspect {

    @Around("@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        long startTime = System.currentTimeMillis();
        log.info("→ {} {}.{}() - START", getHttpMethod(joinPoint), className, methodName);
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("← {} {}.{}() - SUCCESS ({}ms)", getHttpMethod(joinPoint), className, methodName, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("✗ {} {}.{}() - ERROR ({}ms): {}", getHttpMethod(joinPoint), className, methodName, duration, e.getMessage(), e);
            throw e;
        }
    }

    @Around("execution(* com.example.*.service.*.*(..)) && !execution(* com.example.*.service.*.*Health*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        long startTime = System.currentTimeMillis();
        
        // Log with arguments (be careful not to log sensitive data)
        if (args.length > 0 && !containsSensitiveData(methodName)) {
            log.debug("⚙️ {}.{}({}) - START", className, methodName, formatArgs(args));
        } else {
            log.debug("⚙️ {}.{}() - START", className, methodName);
        }
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.debug("⚙️ {}.{}() - SUCCESS ({}ms)", className, methodName, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("⚙️ {}.{}() - ERROR ({}ms): {}", className, methodName, duration, e.getMessage());
            throw e;
        }
    }

    private String getHttpMethod(ProceedingJoinPoint joinPoint) {
        // Simple heuristic - in a real app you'd get this from the request
        String methodName = joinPoint.getSignature().getName().toLowerCase();
        if (methodName.startsWith("get") || methodName.contains("find")) return "GET";
        if (methodName.startsWith("post") || methodName.contains("create")) return "POST";
        if (methodName.startsWith("put") || methodName.contains("update")) return "PUT";
        if (methodName.startsWith("delete")) return "DELETE";
        return "HTTP";
    }

    private boolean containsSensitiveData(String methodName) {
        String lowerMethod = methodName.toLowerCase();
        return lowerMethod.contains("password") || 
               lowerMethod.contains("token") || 
               lowerMethod.contains("secret") ||
               lowerMethod.contains("payment");
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) return "";
        if (args.length == 1) return String.valueOf(args[0]);
        return args.length + " args";
    }
}