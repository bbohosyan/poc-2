package com.sap.aspect;

import com.sap.annotation.LogDatabaseCount;
import com.sap.repository.TableRowRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DatabaseCountLoggingAspect {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseCountLoggingAspect.class);

    @Autowired
    private TableRowRepository repository;

    @Around("@annotation(com.sap.annotation.LogDatabaseCount)")
    public Object logDatabaseCount(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();
        String className = signature.getDeclaringType().getSimpleName();

        LogDatabaseCount annotation = signature.getMethod().getAnnotation(LogDatabaseCount.class);
        String entity = annotation.entity();

        long countBefore = repository.count();
        LOG.info("BEFORE {}.{} | {} count: {}",
                className, methodName, entity, countBefore);

        Object result = joinPoint.proceed();

        long countAfter = repository.count();
        long difference = countAfter - countBefore;

        LOG.info("AFTER  {}.{} | {} count: {} | Change: {}",
                className, methodName, entity, countAfter, difference);

        return result;
    }
}