package com.souche.javaassitagent.interceptor;

import com.souche.soucheagent.common.model.BeforeMethodResult;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/11 上午9:15
 * @Version: 1.0
 * @Description: 
 */
@Slf4j
public abstract  class AgentMethodInterceptor {


    /**
     * 方法执行之前的拦截
     * @param args
     * @param instance
     * @param clazz
     * @param methodName
     * @return Object
     */
    public abstract BeforeMethodResult beforeMethod(Object instance, Class clazz, String methodName, Object[] args);

    /**
     * 方法执行之后的拦截
     * @param args
     * @param instance
     * @param clazz
     * @param methodName
     * @return Object 如果不对返回值做修改的话，则返回 result
     */
    public abstract Object afterMethod(Object instance,Class clazz,String methodName,Object[] args,Object result);

    /**
     * 方法执行异常的拦截
     * @param args
     * @param instance
     * @param clazz
     * @param methodName
     */
    public abstract void handleException(Object instance,Class clazz,String methodName,Object[] args,Throwable e);
}
