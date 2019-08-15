package com.souche.perf.http.interceptor;

import com.souche.soucheagent.common.exception.AgentException;
import com.souche.soucheagent.common.model.BeforeMethodResult;
import com.souche.javaassitagent.interceptor.AgentMethodInterceptor;
import com.souche.soucheagent.common.util.Constants;
import com.souche.soucheagent.common.util.StressTagThreadLocalHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class HttpTomcatRequestInterceptor extends AgentMethodInterceptor {


    public static final HttpTomcatRequestInterceptor instance = new HttpTomcatRequestInterceptor();

    public HttpTomcatRequestInterceptor() {

    }

    public  static HttpTomcatRequestInterceptor getInstance() {
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
            if (StringUtils.isEmpty(StressTagThreadLocalHolder.getTagThreadLocal())) {

                HttpServletRequest request = (HttpServletRequest)args[0];
                String perfTag = request.getHeader(Constants.X_SOUCHE_PERF);

                if (!StringUtils.isEmpty(perfTag)) {
                    StressTagThreadLocalHolder.setTagThreadLocal(perfTag);
                }
                log.info("--------------- HttpTomcatRequestInterceptor  stressTag is =>>>" + perfTag);
            }
        } catch (Throwable e) {
            log.error("fail to execute HttpTomcatRequestInterceptor beforeMethod.",e);
            throw new AgentException("fail to execute HttpTomcatRequestInterceptor beforeMethod." + ExceptionUtils.getStackTrace(e));
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
        log.error("fail to execute HttpTomcatRequestInterceptor interceptor.",e);
    }
}
