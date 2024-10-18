package cn.yiidii.jdx.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequestAspect {

    public final HttpServletRequest request;

    @Pointcut("execution(* cn.yiidii.jdx.controller..*.*(..))")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void before(JoinPoint jp) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        log.debug("地址: {}, 方法: {}, 类方法: {}, 参数: {}",
                request.getRequestURL().toString(),
                request.getMethod(),
                jp.getSignature(),
                JSONObject.toJSON(jp.getArgs()));
    }

    @AfterReturning(pointcut = "pointCut()", returning = "o")
    public void afterReturning(JoinPoint jp, Object o) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        log.debug("地址: {}, 方法: {}, 类方法: {}, 参数: {}, 响应: {}",
                request.getRequestURL().toString(),
                request.getMethod(),
                jp.getSignature(),
                JSONObject.toJSON(jp.getArgs())
                , JSON.toJSONString(o)
        );
    }

    @AfterThrowing(pointcut = "pointCut()", throwing = "e")
    public void AfterThrowing(JoinPoint jp, Exception e) {
        log.debug("地址: {}, 方法: {}, 类方法: {}, 参数: {}, 异常: {}",
                request.getRequestURL().toString(),
                request.getMethod(),
                jp.getSignature(),
                JSONObject.toJSON(jp.getArgs()),
                e.getMessage());
    }
}
