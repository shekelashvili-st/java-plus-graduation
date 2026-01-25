package ru.yandex.practicum.core.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(public * ru.yandex.practicum.core..controller.*.*(..))")
    public void loggableControllerMethods() {
    }

    @Before("loggableControllerMethods()")
    public void logBefore(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        log.info("Entering controller method {}\n with args {}", joinPoint.getStaticPart(), args);
    }

    @AfterReturning(value = "loggableControllerMethods()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        log.info("Exiting controller method {}\n with result {}", joinPoint.getStaticPart(), result);
    }
}
