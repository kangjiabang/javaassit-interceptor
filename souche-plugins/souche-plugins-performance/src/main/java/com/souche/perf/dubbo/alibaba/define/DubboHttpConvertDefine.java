package com.souche.perf.dubbo.alibaba.define;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午3:50
 * @Version: 1.0
 * @Description: 
 */
public class DubboHttpConvertDefine extends AbstractInterceptorDefine {


    private String className = "com.alibaba.dubbo.remoting.rpc.http.codec.HttpRequestConvertHandler";
    private String methodName = "received";

    private String intercetor = "com.souche.perf.dubbo.alibaba.interceptor.DubboHttpConvertInterceptor";
    @Override
    public String matchedClassName() {
        return className;
    }

    @Override
    public String methodName() {
        return methodName;
    }

    @Override
    public String interceptor() {
        return intercetor;
    }

    @Override
    public String[] methodParams() {
        return new String[] {"com.alibaba.dubbo.remoting.Channel","java.lang.Object"};
    }

}
