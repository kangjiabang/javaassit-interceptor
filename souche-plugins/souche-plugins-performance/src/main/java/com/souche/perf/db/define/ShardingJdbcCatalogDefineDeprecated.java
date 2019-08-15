package com.souche.perf.db.define;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午4:15
 * @Version: 1.0
 * @Description: 
 */
public class ShardingJdbcCatalogDefineDeprecated extends AbstractInterceptorDefine {


    private String className = "com.mysql.cj.jdbc.StatementImpl_Deprecated";
    private String methodName = "getCurrentCatalog";

    private String intercetor = "com.souche.perf.db.interceptor.ShardingJdbcCatalogInterceptor";
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
