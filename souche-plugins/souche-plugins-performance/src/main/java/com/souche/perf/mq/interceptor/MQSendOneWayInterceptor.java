package com.souche.perf.mq.interceptor;

import com.souche.soucheagent.common.exception.IgnoreOriginExecuteException;
import com.souche.soucheagent.common.model.BeforeMethodResult;
import com.souche.soucheagent.common.util.StressTagThreadLocalHolder;
import com.souche.javaassitagent.interceptor.AgentMethodInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class MQSendOneWayInterceptor extends AgentMethodInterceptor {


    public static final MQSendOneWayInterceptor instance = new MQSendOneWayInterceptor();

    public MQSendOneWayInterceptor() {

    }

    public static MQSendOneWayInterceptor getInstance() {
        return instance;
    }

    /**
     * 方法执行之前的拦截
     *
     * @param args
     * @param instance
     * @param clazz
     * @param methodName
     */
    @Override
    public BeforeMethodResult beforeMethod(Object instance, Class clazz, String methodName, Object[] args) {
        try {
            if (!StringUtils.isEmpty(StressTagThreadLocalHolder.getTagThreadLocal())) {

                log.info("--------------- MQSendOneWayInterceptor  ignore send MQ for perftest");

                return new BeforeMethodResult(true,null);

                /*Message message = (Message)allArguments[0];

                message.putUserProperties(Constants.X_SOUCHE_PERF, StressTagThreadLocalHolder.getTagThreadLocal());

                log.info("--------------- MQSendInterceptor  stressTag is =>>>" + StressTagThreadLocalHolder.getTagThreadLocal());*/
            }
        } catch (Throwable e) {

            if (e instanceof IgnoreOriginExecuteException) {
                throw (RuntimeException) e;
            }
            log.error("fail to execute MQSendOneWayInterceptor beforeMethod.", e);
        }
        return null;
    }

    /**
     * 方法执行之后的拦截
     *
     * @param args
     * @param instance
     * @param clazz
     * @param methodName
     * @return Object 返回值
     */
    @Override
    public Object afterMethod(Object instance, Class clazz, String methodName, Object[] args, Object result) {
        return result;
    }

    @Override
    public void handleException(Object instance, Class clazz, String methodName, Object[] args, Throwable e) {
        log.error("fail to execute MQSendOneWayInterceptor interceptor.", e);
    }
}
