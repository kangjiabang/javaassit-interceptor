package com.souche.perf.http.define;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;

public class OKHttpClientRequestDefine extends AbstractInterceptorDefine {


    private String className = "okhttp3.OkHttpClient";
    private String methodName = "newCall";

    private String intercetor = "com.souche.perf.http.interceptor.OKHttpClientRequestInterceptor";
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
        return new String[] {"okhttp3.Request"};
    }
}
