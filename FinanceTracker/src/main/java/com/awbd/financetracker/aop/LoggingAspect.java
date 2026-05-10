package com.awbd.financetracker.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("within(com.awbd.financetracker.service..*)")
    public void serviceLayer() {}

    @Around("serviceLayer()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        String className  = pjp.getSignature().getDeclaringType().getSimpleName();
        String methodName = pjp.getSignature().getName();

        log.debug(">> {}.{}() args={}", className, methodName, Arrays.toString(pjp.getArgs()));

        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            log.debug("<< {}.{}() completed in {} ms", className, methodName,
                    System.currentTimeMillis() - start);
            return result;
        } catch (Exception ex) {
            log.error("FAILED {}.{}() threw {}: {}", className, methodName,
                    ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }
}
