package com.souche.perf.db.define;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午4:15
 * @Version: 1.0
 * @Description: 
 */
public class MybatisDataSourceDefine extends AbstractInterceptorDefine {


    private String className = "org.apache.ibatis.mapping.Environment";
    private String methodName = "getDataSource";

    private String intercetor = "com.souche.perf.db.interceptor.MybatisDataSourceInterceptor";
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
        return new String[0];
    }

}
