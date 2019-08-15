package com.souche.perf.http.define;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;

public class HttpClientRequestDefine extends AbstractInterceptorDefine {


    private String className = "org.apache.http.impl.client.CloseableHttpClient";
    private String methodName = "execute";

    private String intercetor = "com.souche.perf.http.interceptor.HttpClientRequestInterceptor";
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
        return new String[] {"org.apache.http.client.methods.HttpUriRequest"};
    }

}
