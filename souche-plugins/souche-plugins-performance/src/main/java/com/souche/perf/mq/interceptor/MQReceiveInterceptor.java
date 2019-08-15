package com.souche.perf.mq.interceptor;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.MessageExt;
import com.souche.soucheagent.common.model.BeforeMethodResult;
import com.souche.javaassitagent.interceptor.AgentMethodInterceptor;
import com.souche.soucheagent.common.util.Constants;
import com.souche.soucheagent.common.util.StressTagThreadLocalHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Slf4j
public class MQReceiveInterceptor extends AgentMethodInterceptor {


    public static final MQReceiveInterceptor instance = new MQReceiveInterceptor();

    public MQReceiveInterceptor() {

    }

    public  static MQReceiveInterceptor getInstance() {
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

            List<MessageExt> msgLists = (List< MessageExt >) args[0];

            MessageExt messageExt = msgLists.get(0);
            //将压测标设置到上下文中
            if (StringUtils.isNotBlank(messageExt.getProperty(Constants.X_SOUCHE_PERF))) {
                StressTagThreadLocalHolder.setTagThreadLocal(messageExt.getProperty(Constants.X_SOUCHE_PERF));
                log.info("--------------- MQReceiveInterceptor  stressTag is =>>>" + StressTagThreadLocalHolder.getTagThreadLocal());
            }
        } catch (Throwable e) {
            log.error("fail to execute MQReceiveInterceptor beforeMethod.", e);
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
        log.error("fail to execute MQReceiveInterceptor interceptor.",e);
    }
}
