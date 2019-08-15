package com.souche.perf.mock.handler;

import com.alibaba.fastjson.JSONObject;
import com.souche.perf.mock.HttpResponseProxy;
import com.souche.perf.mock.exception.MockException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicHttpResponse;

import java.io.ByteArrayInputStream;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/26 上午9:05
 * @Version: 1.0
 * @Description:
 */
@Slf4j
public class HttpResultHandler  implements ResultHandler<CloseableHttpResponse> {


    @Override
    public CloseableHttpResponse handle(String responseBody) {

        JSONObject jsonObject = JSONObject.parseObject(responseBody);

        return getMockHttpCloseableResponse(jsonObject.getString("data"));
    }


    /**
     * 获取http的结果
     *
     * @param result
     * @return
     */
    public CloseableHttpResponse getMockHttpCloseableResponse(String result) {

        try {

            BasicHttpResponse basicHttpResponse = (BasicHttpResponse) new DefaultHttpResponseFactory().newHttpResponse(HttpVersion.HTTP_1_1,
                    HttpStatus.SC_OK, null);


            BasicHttpEntity httpEntity = new BasicHttpEntity();

            httpEntity.setContent(new ByteArrayInputStream(result.getBytes()));
            httpEntity.setContentLength(result.length());
            //设置自定义的entity
            basicHttpResponse.setEntity(httpEntity);

            return new HttpResponseProxy(basicHttpResponse,null);

        } catch (Exception e) {
            log.error("fail to getMockHttpCloseableResponse.", e);
            throw new MockException("fail to getMockHttpCloseableResponse.", e);
        }

    }
}
