package kr.co.seoulit.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class LoggerAspect {

    @Around("execution(* kr.co.seoulit..controller..*Controller.*(..))"
            + " or execution(* kr.co.seoulit..service..*Impl.*(..))"
            + " or execution(* kr.co.seoulit..mapper..*Mapper.*(..))")
    public Object logPrint(ProceedingJoinPoint joinPoint) throws Throwable {
        String type = "";
        String name = joinPoint.getSignature().getDeclaringTypeName();

        if (name.contains("Controller")) {
            type = "Controller  : ";
        } else if (name.contains("Service")) {
            type = "서비스    : ";
        } else if (name.contains("Mapper")) {
            type = "매퍼      : ";
        }

        String method = name + "." + joinPoint.getSignature().getName() + "()";
        log.info(type + method + " 시작");
        Object obj = joinPoint.proceed();
        log.info(type + method + " 종료");
        return obj;
    }
}


