package com.souche.perf.http.define;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;

public class HttpTomcatRequestDefine extends AbstractInterceptorDefine {


    private String className = "org.apache.catalina.core.StandardHostValve";
    private String methodName = "invoke";

    private String intercetor = "com.souche.perf.http.interceptor.HttpTomcatRequestInterceptor";
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
        return new String[] {"org.apache.catalina.connector.Request","org.apache.catalina.connector.Response"};
    }
}
