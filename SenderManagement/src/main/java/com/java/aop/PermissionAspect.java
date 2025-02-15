package com.java.aop;


import com.java.annotation.AuthPermission;
import com.java.common.Response.Result;
import com.java.common.Response.ResultCode;
import com.java.common.userCommon.AuthValidation;
import com.java.common.userCommon.ThreadLocalUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对请求进行用户权限验证
 */
@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private AuthValidation authValidation;

    // 使用缓存存储方法的权限信息
    private static final Map<Method, String> PERMISSION_CACHE = new ConcurrentHashMap<>();

    // 获取权限参数方法
    private String getPermission(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 先从缓存中获取
        String permission = PERMISSION_CACHE.get(method);
        if (permission != null) {
            return permission;
        }

        // 缓存中没有，则解析注解
        AuthPermission annotation = method.getAnnotation(AuthPermission.class);
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(AuthPermission.class);
        }

        permission = annotation != null ? annotation.permission() : "";
        // 存入缓存
        PERMISSION_CACHE.put(method, permission);
        return permission;
    }

    @Around("@within(com.java.annotation.AuthPermission) || @annotation(com.java.annotation.AuthPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        String userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            return new Result(ResultCode.R_Error);
        }

        // 获取权限参数
        String permission = getPermission(joinPoint);
        if (permission == null) {
            return new Result(ResultCode.R_Error);
        }

        // 进行权限验证
        if (!authValidation.checkAuth(permission, userId)) {
            return new Result(ResultCode.R_NoAuth);
        }

        // 验证通过，继续执行原方法
        return joinPoint.proceed();
    }
} 