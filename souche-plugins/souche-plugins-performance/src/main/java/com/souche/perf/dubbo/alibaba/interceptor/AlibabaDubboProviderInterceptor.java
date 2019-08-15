package com.souche.perf.dubbo.alibaba.interceptor;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.RpcContext;
import com.souche.soucheagent.common.exception.AgentException;
import com.souche.soucheagent.common.model.BeforeMethodResult;
import com.souche.soucheagent.common.util.Constants;
import com.souche.soucheagent.common.util.StressTagThreadLocalHolder;
import com.souche.javaassitagent.interceptor.AgentMethodInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午3:38
 * @Version: 1.0
 * @Description: 
 */
@Slf4j
public class AlibabaDubboProviderInterceptor extends AgentMethodInterceptor {


    public static final AlibabaDubboProviderInterceptor instance = new AlibabaDubboProviderInterceptor();

    public AlibabaDubboProviderInterceptor() {

    }

    public  static AlibabaDubboProviderInterceptor getInstance() {
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
            String stressTag = ((Invocation)args[1]).getAttachment(Constants.X_SOUCHE_PERF);

            if (StringUtils.isEmpty(stressTag)){
                stressTag = StressTagThreadLocalHolder.getTagThreadLocal();
            }

            if (!StringUtils.isEmpty(stressTag)){
                StressTagThreadLocalHolder.setTagThreadLocal(stressTag);
                RpcContext.getContext().setAttachment(Constants.X_SOUCHE_PERF,stressTag);
            }

            log.info("--------------- AlibabaDubboProviderInterceptor invoke dubbo.Stresstag is =>>>" + stressTag);

        } catch (Throwable e) {
            log.error("fail to execute AlibabaDubboProviderInterceptor beforeMethod.",e);
            throw new AgentException("fail to execute AlibabaDubboProviderInterceptor beforeMethod." + ExceptionUtils.getStackTrace(e));
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
        log.error("fail to execute AlibabaDubboProviderInterceptor interceptor.",e);
    }
}
