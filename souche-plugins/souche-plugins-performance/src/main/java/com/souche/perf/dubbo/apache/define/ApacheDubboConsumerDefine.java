package com.souche.perf.dubbo.apache.define;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午3:50
 * @Version: 1.0
 * @Description: 
 */
public class ApacheDubboConsumerDefine extends AbstractInterceptorDefine {


    private String className = "org.apache.dubbo.rpc.proxy.InvokerInvocationHandler";
    private String methodName = "preTrace";

    private String intercetor = "com.souche.perf.dubbo.apache.interceptor.ApacheDubboConsumeInterceptor";
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
        return new String[] {"org.apache.dubbo.rpc.RpcInvocation"};
    }

}
