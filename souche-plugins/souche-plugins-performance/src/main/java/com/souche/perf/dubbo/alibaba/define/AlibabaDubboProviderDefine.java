package com.souche.perf.dubbo.alibaba.define;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午4:47
 * @Version: 1.0
 * @Description: 
 */
public class AlibabaDubboProviderDefine extends AbstractInterceptorDefine {


    private String className = "com.alibaba.dubbo.rpc.filter.ClassLoaderFilter";
    private String methodName = "invoke";

    private String intercetor = "com.souche.perf.dubbo.alibaba.interceptor.AlibabaDubboProviderInterceptor";
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
        return new String[] {"com.alibaba.dubbo.rpc.Invoker","com.alibaba.dubbo.rpc.Invocation"};
    }

}
