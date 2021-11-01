package net.atos.frenchcitizen.aop.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@Aspect
public class LoggingAspect {


    /**
     * Pointcut that matches all Spring beans in the application's main packages.
     */
    @Pointcut("within(net.atos.frenchcitizen.service..*)" +
            " || within(net.atos.frenchcitizen.controller..*)")
    public void applicationPackagePointcut() {
    }

    /**
     * Retrieves the {@link Logger} associated to the given {@link JoinPoint}.
     *
     * @param joinPoint join point we want the logger for.
     * @return {@link Logger} associated to the given {@link JoinPoint}.
     */
    private Logger logger(JoinPoint joinPoint) {
        return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
    }

    /**
     * Advice that logs when a method is entered and exited.
     *
     * @param joinPoint join point for advice.
     * @return result.
     * @throws Throwable throws {@link IllegalArgumentException}.
     */
    @Around("applicationPackagePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = logger(joinPoint);

        String arguments = Arrays.toString(joinPoint.getArgs());
        Instant enterMethod = Instant.now();
        log.info("Enter: {}() with argument[s] = {}", joinPoint.getSignature().getName(), arguments);

        try {
            Object result = joinPoint.proceed();
            Instant exitMethod = Instant.now();
            log.info("Exit: {}() with result = {} in {}ms", joinPoint.getSignature().getName(), result, Duration.between(enterMethod, exitMethod).toMillis());
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {} in {}()", Arrays.toString(joinPoint.getArgs()), joinPoint.getSignature().getName());
            throw e;
        }
    }
}

