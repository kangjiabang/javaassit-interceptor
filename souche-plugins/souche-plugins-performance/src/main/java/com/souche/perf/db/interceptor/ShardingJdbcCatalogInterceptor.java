package com.souche.perf.db.interceptor;

import com.souche.javaassitagent.interceptor.AgentMethodInterceptor;
import com.souche.soucheagent.common.model.BeforeMethodResult;
import com.souche.soucheagent.common.util.Constants;
import com.souche.soucheagent.common.util.StressTagThreadLocalHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午3:48
 * @Version: 1.0
 * @Description:
 */
@Slf4j
public class ShardingJdbcCatalogInterceptor extends AgentMethodInterceptor {


    public static final ShardingJdbcCatalogInterceptor instance = new ShardingJdbcCatalogInterceptor();


    public ShardingJdbcCatalogInterceptor() {
    }

    public static ShardingJdbcCatalogInterceptor getInstance() {
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

        //如果不带压测的标，直接返回
        if (StringUtils.isEmpty(StressTagThreadLocalHolder.getTagThreadLocal())) {
            return result;
        }
        result = result + Constants.SHADOW_SUFFIX;

        return result;

    }

    @Override
    public void handleException(Object instance, Class clazz, String methodName, Object[] args, Throwable e) {
        log.error("fail to execute AlibabaDubboConsumeInterceptor interceptor.", e);
    }
}
