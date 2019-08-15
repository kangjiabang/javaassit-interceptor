package com.souche.perf.dubbo.alibaba.interceptor;

import com.souche.javaassitagent.interceptor.AgentMethodInterceptor;
import com.souche.perf.mock.exception.MockException;
import com.souche.perf.mock.MockResult;
import com.souche.perf.mock.MockService;
import com.souche.soucheagent.common.exception.AgentException;
import com.souche.soucheagent.common.model.BeforeMethodResult;
import com.souche.soucheagent.common.util.StressTagThreadLocalHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Method;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午3:48
 * @Version: 1.0
 * @Description: 
 */
@Slf4j
public class AlibabaDubboConsumeInvokeInterceptor extends AgentMethodInterceptor {


    public static final AlibabaDubboConsumeInvokeInterceptor instance = new AlibabaDubboConsumeInvokeInterceptor();

    private MockService mockService;
    public AlibabaDubboConsumeInvokeInterceptor() {
        mockService = MockService.getInstance();
    }

    public  static AlibabaDubboConsumeInvokeInterceptor getInstance() {
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

                String interfaceName = ((Method)args[1]).getDeclaringClass().getName();

                String httpPath = interfaceName.replace('.','/');
                //如果是mock接口，从mock平台查询mock结果，并返回
                if (mockService.isMockInterface(httpPath,true,MockService.DUBBO)) {

                    long startTime = System.currentTimeMillis();


                    MockResult mockResult = mockService.getHttpMockResult(httpPath,true,MockService.DUBBO);

                    if (mockResult == null || mockResult.getResponse() == null) {
                        throw new MockException(String.format("fail to query dubbo interface:%s result,please configure it.",interfaceName));
                    }

                    long timeSpent = System.currentTimeMillis() - startTime;

                    long spentTimeInMill = mockResult.getSpentTimeInMill();

                    //接口需要mock调用时间，并且调用时长小于需要mock的时长
                    if (spentTimeInMill != 0 && timeSpent < spentTimeInMill ) {
                        Thread.sleep((spentTimeInMill - timeSpent));
                    }
                    return new BeforeMethodResult(true,mockResult.getResponse());
                }
            }
            log.info("--------------- AlibabaDubboConsumeInvokeInterceptor invoke dubbo.Stresstag is =>>>" + StressTagThreadLocalHolder.getTagThreadLocal());

        } catch (Throwable e) {
            log.error("fail to execute AlibabaDubboConsumeInvokeInterceptor beforeMethod.",e);

            throw new AgentException("fail to execute AlibabaDubboConsumeInvokeInterceptor beforeMethod." + ExceptionUtils.getStackTrace(e));
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
        log.error("fail to execute AlibabaDubboConsumeInvokeInterceptor interceptor.",e);
    }
}
