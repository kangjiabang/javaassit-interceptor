package com.souche.perf.mock;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.souche.perf.mock.exception.MockException;
import com.souche.perf.mock.exception.MockUrlNotFoundException;
import com.souche.perf.mock.handler.DubboResultHandler;
import com.souche.perf.mock.handler.HttpResultHandler;
import com.souche.perf.mock.handler.ResultHandler;
import com.souche.soucheagent.common.util.StressTagThreadLocalHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/24 下午3:38
 * @Version: 1.0
 * @Description:
 */
@Slf4j
public class MockService {


    private final static MockService instance = new MockService();

    public static final int HTTP = 1;
    public static final int DUBBO = 2;

    private Map<Integer, ResultHandler> mockTypeToResultHandlers = new HashMap<>();


    CloseableHttpClient httpclient = HttpClients.createDefault();

    private Map<String, MockResult> pathToMockResultMap = new ConcurrentHashMap<>();

    private ReentrantLock reentrantLock = new ReentrantLock();

    //private Map<String, Boolean> interfaceIsMockMap = new ConcurrentHashMap<>();

    // 通过CacheBuilder构建一个缓存实例
    private  Cache<String, Boolean> interfaceIsMockMap = CacheBuilder.newBuilder()
            .maximumSize(1000) // 设置缓存的最大容量
            .expireAfterWrite(2, TimeUnit.MINUTES) // 设置缓存在写入一分钟后失效
            .build();

    private MockService() {
        loadAllMockInterfaces();

        mockTypeToResultHandlers.put(HTTP, new HttpResultHandler());
        mockTypeToResultHandlers.put(DUBBO, new DubboResultHandler());
    }


    public static MockService getInstance() {
        return instance;
    }


    /**
     * 加载所有的mock接口配置
     */
    private void loadAllMockInterfaces() {

    }

    /**
     * 从mock平台获取mock的结果
     *
     * @param httpPath
     * @param isPost
     * @param type     HTTP or DUBBO
     * @return
     */
    public MockResult getHttpMockResult(String httpPath, boolean isPost, int type) {


        HttpRequestBase method = null;
        String mockProjectId = HttpClientHelper.getMockProjectId();

        String fullHttpPath = HttpClientHelper.getFullHttpPath(httpPath, mockProjectId);

        //缓存不为空，设置到缓存中
        if (pathToMockResultMap.get(fullHttpPath) != null) {
            //对于http请求，closeableHttpresponse 需要重新构造
            if (type == HTTP) {
                MockResult mockResult = pathToMockResultMap.get(fullHttpPath);
                String data = mockResult.getData();
                Object response = mockTypeToResultHandlers.get(HTTP).handle(data);
                mockResult.setResponse(response);
                return mockResult;
            }
            return pathToMockResultMap.get(fullHttpPath);
        }

        method = HttpClientHelper.getHttpRequestBase(isPost, fullHttpPath);

        try {

            ResponseHandler<String> responseHandler = HttpClientHelper.getResponseHandler();

            /**
             * {
             "success": true,
             "spentTimeInMill": 3000,
             "className":"com.xxx.response",
             "data": {
             "result": "OK"
             }
             }
             */
            String responseBody = httpclient.execute(method, responseHandler);

            JSONObject jsonObject = JSONObject.parseObject(responseBody);

            Object result = mockTypeToResultHandlers.get(type).handle(responseBody);

            MockResult<Object> mockResult = new MockResult(jsonObject.getIntValue("spentTimeInMill"), responseBody, result);

            pathToMockResultMap.put(fullHttpPath, mockResult);

            return mockResult;

        } catch (Exception e) {
            log.error("fail to execute http query.", e);
            throw new MockException("fail to execute http query.", e);
        } finally {

        }

    }


    /**
     * @param httpPath
     * @param type     接口类型
     * @return
     */
    public boolean isMockInterface(String httpPath, boolean isPost, int type) {

        if (interfaceIsMockMap.getIfPresent(httpPath) != null) {
            return interfaceIsMockMap.getIfPresent(httpPath);
        }

        try {
            //加锁
            reentrantLock.lock();

            //再次判断是否为空
            if (interfaceIsMockMap.getIfPresent(httpPath) != null) {
                return interfaceIsMockMap.getIfPresent(httpPath);
            }

            String fullHttpPath = HttpClientHelper.getFullHttpPath(httpPath, HttpClientHelper.getMockProjectId());

            HttpRequestBase method = HttpClientHelper.getHttpRequestBase(isPost, fullHttpPath);

            ResponseHandler<String> responseHandler = HttpClientHelper.getResponseHandler();

            /**
             * {
             "success": true,
             "spentTimeInMill": 3000,
             "className":"com.xxx.response",
             "data": {
             "result": "OK"
             }
             }
             */
            //通过执行http请求是否返回404判断是否是mock接口
            httpclient.execute(method, responseHandler);
            interfaceIsMockMap.put(httpPath, true);
            log.info("httpath:{} is  mock interface.", httpPath);
            return true;
        } catch (Exception e) {
            if (e instanceof MockUrlNotFoundException) {
                log.info("httpath:{} is not mock interface.", httpPath);
                interfaceIsMockMap.put(httpPath, false);
                return false;
            }
            log.error("fail to execute isMockInterface.", e);
            throw new MockException("fail to execute isMockInterface.", e);
        } finally {
            reentrantLock.unlock();
        }


}


}
