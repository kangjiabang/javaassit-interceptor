package com.souche.perf.http.interceptor;

import com.souche.soucheagent.common.exception.AgentException;
import com.souche.soucheagent.common.model.BeforeMethodResult;
import com.souche.javaassitagent.interceptor.AgentMethodInterceptor;
import com.souche.soucheagent.common.util.Constants;
import com.souche.soucheagent.common.util.StressTagThreadLocalHolder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Slf4j
public class OKHttpClientRequestInterceptor extends AgentMethodInterceptor {


    public static final OKHttpClientRequestInterceptor instance = new OKHttpClientRequestInterceptor();

    public OKHttpClientRequestInterceptor() {

    }

    public  static OKHttpClientRequestInterceptor getInstance() {
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
            if (!StringUtils.isEmpty(StressTagThreadLocalHolder.getTagThreadLocal())) {

                Request request = (Request)args[0];

                Field headersField = Request.class.getDeclaredField("headers");
                headersField.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.set(headersField,headersField.getModifiers() & ~Modifier.FINAL);

                Headers headers = request.headers();
                Headers newHeaders = headers.newBuilder().add(Constants.X_SOUCHE_PERF,Constants.X_SOUCHE_PERF).build();

                headersField.set(request,newHeaders);

                log.info("--------------- OkHttpRequestInterceptor  stressTag is =>>>" + request.headers().get(Constants.X_SOUCHE_PERF));
            }
        } catch (Throwable e) {
            log.error("fail to execute OkHttpRequestInterceptor beforeMethod.",e);

            throw new AgentException("fail to execute OkHttpRequestInterceptor beforeMethod." + ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    /**
     * 方法执行之后的拦截
     * @param args
     * @param instance
     * @param clazz
     * @param methodName
     * @return Object 返回值
     */
    @Override
    public Object afterMethod(Object instance,Class clazz,String methodName,Object[] args,Object result) {
        return result;
    }

    @Override
    public void handleException(Object instance, Class clazz, String methodName, Object[] args, Throwable e) {
        log.error("fail to execute OKHttpClientRequestInterceptor interceptor.",e);
    }
}
