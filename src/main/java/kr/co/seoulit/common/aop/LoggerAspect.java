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
<<<<<<< HEAD:src/main/java/kr/co/seoulit/common/aop/LoggerAspect.java
            type = "ServiceImpl  : ";
        } else if (name.contains("Mapper")) {
            type = "Mapper  : ";
=======
            type = "서비스    : ";
        } else if (name.contains("Mapper")) {
            type = "매퍼      : ";
>>>>>>> 4ce2497 (- 접수 핵심 4개 + 확장 테이블 연동 로직 반영):src/main/java/kr/co/seoulit/reception/common/aop/LoggerAspect.java
        }

        String method = name + "." + joinPoint.getSignature().getName() + "()";
        log.info(type + method + " 시작");
        Object obj = joinPoint.proceed();
        log.info(type + method + " 종료");
        return obj;
    }
}


