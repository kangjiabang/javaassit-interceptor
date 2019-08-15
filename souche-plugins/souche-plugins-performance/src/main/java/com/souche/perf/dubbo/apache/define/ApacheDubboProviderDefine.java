package com.souche.perf.dubbo.apache.define;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午3:47
 * @Version: 1.0
 * @Description: 
 */
public class ApacheDubboProviderDefine extends AbstractInterceptorDefine {


    private String className = "org.apache.dubbo.rpc.filter.ClassLoaderFilter";
    private String methodName = "invoke";

    private String intercetor = "com.souche.perf.dubbo.apache.interceptor.ApacheDubboProviderInterceptor";
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
        return new String[] {"org.apache.dubbo.rpc.Invoker","org.apache.dubbo.rpc.Invocation"};
    }

}
