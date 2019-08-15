package com.souche.perf.dubbo.alibaba.interceptor;

import com.alibaba.dubbo.rpc.RpcContext;
import com.souche.soucheagent.common.exception.AgentException;
import com.souche.soucheagent.common.model.BeforeMethodResult;
import com.souche.soucheagent.common.util.Constants;
import com.souche.soucheagent.common.util.StressTagThreadLocalHolder;
import com.souche.javaassitagent.interceptor.AgentMethodInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午3:31
 * @Version: 1.0
 * @Description: 
 */
@Slf4j
public class DubboHttpConvertInterceptor extends AgentMethodInterceptor {


    public static final DubboHttpConvertInterceptor instance = new DubboHttpConvertInterceptor();

    public DubboHttpConvertInterceptor() {

    }

    public  static DubboHttpConvertInterceptor getInstance() {
        return instance;
    }

    /**
     * 方法执行之前的拦截
     * @param args
     * @param instance
     * @param clazz
     * @param methodName
     */
    @Override
    public BeforeMethodResult beforeMethod(Object instance, Class clazz, String methodName, Object[] args) {
        try {
            Object message = args[1];
            if (!(message instanceof HttpRequest)) {
                return null;
            }
            String stressTag = null;
            if (HttpMethod.POST.equals(((HttpRequest) message).getMethod())) {
                HttpRequest httpRequest = (HttpRequest) message;
                HttpHeaders headers = httpRequest.headers();
                stressTag = headers.get(Constants.X_SOUCHE_PERF);
            }
            if (StringUtils.isEmpty(stressTag)) {
                stressTag = StressTagThreadLocalHolder.getTagThreadLocal();
            }

            if (!StringUtils.isEmpty(stressTag)) {
                StressTagThreadLocalHolder.setTagThreadLocal(stressTag);
                RpcContext.getContext().setAttachment(Constants.X_SOUCHE_PERF, stressTag);
            }
            log.info("--------------- DubboHttpConvertInterceptor set dubbo http request tag is =>>>" + stressTag);

        } catch (Throwable e) {
            log.error("fail to execute DubboHttpConvertInterceptor handleMethodException.",e);
            throw new AgentException("fail to execute DubboHttpConvertInterceptor beforeMethod." + ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    /**
     * 方法执行之后的拦截
     * @param args
     * @param instance
     * @param clazz
     * @param methodName
     *
     * @return Object 返回值
     */
    @Override
    public Object afterMethod(Object instance,Class clazz,String methodName,Object[] args,Object result) {
        return result;
    }

    @Override
    public void handleException(Object instance, Class clazz, String methodName, Object[] args, Throwable e) {
        log.error("fail to execute DubboHttpConvertInterceptor interceptor.",e);
    }
}
