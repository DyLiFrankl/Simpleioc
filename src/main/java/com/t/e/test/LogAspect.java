package com.t.e.test;

import com.t.e.aop.*;
import com.t.e.simpleioc.annotations.Component;

@Aspect
@Component
public class LogAspect {
    @Before("execution(* com.t.e.service.UserService.*(..))")
    public void beforeAdvice(ProceedingJoinPoint pjp) {
        System.out.println("[Before] Method: " + pjp.getMethodName());
    }

    @After("execution(* com.t.e.service.UserService.*(..))")
    public void afterAdvice(ProceedingJoinPoint pjp) {
        System.out.println("[After] Method executed");
    }

    @AfterThrowing("execution(* com.t.e.service.UserService.*(..))")
    public void afterThrowing(Throwable ex) {
        System.out.println("[AfterThrowing] Exception: " + ex.getMessage());
    }
}
