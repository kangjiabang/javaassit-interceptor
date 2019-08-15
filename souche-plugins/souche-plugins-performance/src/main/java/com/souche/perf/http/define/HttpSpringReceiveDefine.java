package com.souche.perf.http.define;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;

public class HttpSpringReceiveDefine extends AbstractInterceptorDefine {


    private String className = "org.springframework.web.servlet.DispatcherServlet";
    private String methodName = "doService";

    private String intercetor = "com.souche.perf.http.interceptor.HttpSpringReceiveInterceptor";
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
        return new String[] {"javax.servlet.http.HttpServletRequest","javax.servlet.http.HttpServletResponse"};
    }
}
