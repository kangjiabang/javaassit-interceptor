package com.souche.perf.mock.handler;

import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.fastjson.JSONObject;
import com.souche.perf.mock.exception.MockException;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/26 上午9:19
 * @Version: 1.0
 * @Description:
 */
@Slf4j
public class DubboResultHandler implements ResultHandler<Object> {


    /**
     *
     * @param responseBody  {
                                "success": true,
                                "spentTimeInMill": 3000,
                                "className":"com.xxx.response",
                                "data": {
                                "result": "OK"
                                }
                            }
     * @return
     */
    @Override
    public Object handle(String responseBody) {

        JSONObject jsonObject = JSONObject.parseObject(responseBody);

        return getDubboInvokeResult(jsonObject);

    }

    /**
     * 获取mock的dubbo返回结果
     * @param jsonObject
     * @return
     */
    private Object getDubboInvokeResult(JSONObject jsonObject) {
        try {
            //获取className
            String className = jsonObject.getString("className");

            //获取data
            String data = jsonObject.getString("data");

            return JSON.parse(data,Class.forName(className,true,this.getClass().getClassLoader()));
        } catch (Exception e) {
            log.error("fail to get dubboInvokeResult.",e);
            throw new MockException("fail to get dubboInvokeResult.",e);
        }

    }
}
