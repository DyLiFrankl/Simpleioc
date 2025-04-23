package com.t.e.test;

import com.t.e.aop.AfterThrowing;
import com.t.e.aop.Around;
import com.t.e.aop.Aspect;
import com.t.e.aop.ProceedingJoinPoint;
import com.t.e.simpleioc.annotations.Component;


@Aspect
@Component
public class TransactionAspect {
    @Around("execution(* com.t.e.service.UserService.*(..))")
    public Object manageTransaction(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("[Around] 开启事务");
        Object result = pjp.proceed();
        System.out.println("[Around] 提交事务");
        return result;
    }
}