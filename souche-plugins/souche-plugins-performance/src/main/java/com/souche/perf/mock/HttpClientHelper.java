package com.souche.perf.mock;

import com.souche.perf.mock.exception.MockUrlNotFoundException;
import com.souche.soucheagent.common.util.StressTagThreadLocalHolder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/26 下午3:14
 * @Version: 1.0
 * @Description: 
 */
public class HttpClientHelper {



    private final static String HTTP_PREFIX = "https://mock.souche-inc.com/mock/";


    /**
     * 
     * @param httpPath
     * @param mockProjectId
     * @return
     */
    public static String getFullHttpPath(String httpPath, String mockProjectId) {
        return HTTP_PREFIX + mockProjectId + "/" +  httpPath;
    }

    /**
     * 
     * @return
     */
    public static String getMockProjectId() {
        //mock的项目id取压测标的value值
        return StressTagThreadLocalHolder.getTagThreadLocal();
    }

    /**
     * 
     * @return
     * @throws IOException
     */
    public static ResponseHandler<String> getResponseHandler() throws IOException {
        // Create a custom response handler
        return new ResponseHandler<String>() {

            @Override
            public String handleResponse(
                    final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                }  else if (status == 404) {
                    throw new MockUrlNotFoundException("not mock interface");
                }
                else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }

        };
    }

    /**
     * 
     * @param isPost
     * @param fullHttpPath
     * @return
     */
    public static HttpRequestBase getHttpRequestBase(boolean isPost, String fullHttpPath) {
        HttpRequestBase method;
        if (isPost) {
            method = new HttpPost(fullHttpPath);
        } else {
            method = new HttpGet(fullHttpPath);
        }
        return method;
    }
}
