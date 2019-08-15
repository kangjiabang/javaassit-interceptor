package com.souche.perf.dubbo.alibaba.define;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;
/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午4:15
 * @Version: 1.0
 * @Description: 
 */
public class AlibabaDubboConsumerDefine extends AbstractInterceptorDefine {


    private String className = "com.alibaba.dubbo.rpc.proxy.InvokerInvocationHandler";
    private String methodName = "preTrace";

    private String intercetor = "com.souche.perf.dubbo.alibaba.interceptor.AlibabaDubboConsumeInterceptor";
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
        return new String[] {"com.alibaba.dubbo.rpc.RpcInvocation"};
    }

}
