package com.souche.perf.http.interceptor;

import com.souche.javaassitagent.interceptor.AgentMethodInterceptor;
import com.souche.perf.mock.MockResult;
import com.souche.perf.mock.MockService;
import com.souche.soucheagent.common.exception.AgentException;
import com.souche.soucheagent.common.model.BeforeMethodResult;
import com.souche.soucheagent.common.util.Constants;
import com.souche.soucheagent.common.util.StressTagThreadLocalHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

@Slf4j
public class HttpClientRequestInterceptor extends AgentMethodInterceptor {


    public static final HttpClientRequestInterceptor instance = new HttpClientRequestInterceptor();

    private  MockService mockService = MockService.getInstance();

    public HttpClientRequestInterceptor() {

    }

    public  static HttpClientRequestInterceptor getInstance() {
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

                HttpUriRequest request = (HttpUriRequest)args[0];
                request.setHeader(Constants.X_SOUCHE_PERF, StressTagThreadLocalHolder.getTagThreadLocal());

                //TODO 确认是否这样获取
                String requestName = request.getURI().getPath();
                //判断是否是mock接口
                if (mockService.isMockInterface(requestName,request.getMethod().equalsIgnoreCase("post"),MockService.HTTP)) {

                    long startTime = System.currentTimeMillis();

                    MockResult mockResult = mockService.getHttpMockResult(requestName, request.getMethod().equalsIgnoreCase("post"),MockService.HTTP);

                    long timeSpent = System.currentTimeMillis() - startTime;

                    long spentTimeInMill = mockResult.getSpentTimeInMill();

                    //接口需要mock调用时间，并且调用时长小于需要mock的时长
                    if (spentTimeInMill != 0 && timeSpent < spentTimeInMill ) {
                        Thread.sleep((spentTimeInMill - timeSpent));
                    }

                    return new BeforeMethodResult(true,mockResult.getResponse());
                }
            }
            log.info("--------------- HttpClientRequestInterceptor  stressTag is =>>>" + StressTagThreadLocalHolder.getTagThreadLocal());
        } catch (Throwable e) {
            log.error("fail to execute HttpClientRequestInterceptor beforeMethod.",e);
            throw new AgentException("fail to execute HttpClientRequestInterceptor beforeMethod." + ExceptionUtils.getStackTrace(e));
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
        log.error("fail to execute HttpClientRequestInterceptor interceptor.",e);
    }
}
