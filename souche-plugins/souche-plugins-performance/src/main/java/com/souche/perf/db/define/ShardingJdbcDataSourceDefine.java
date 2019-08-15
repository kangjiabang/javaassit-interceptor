package com.souche.perf.db.define;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午4:15
 * @Version: 1.0
 * @Description: 
 */
public class ShardingJdbcDataSourceDefine extends AbstractInterceptorDefine {


    private String className = "org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource";

    private String methodName = "getConnection";

    private String intercetor = "com.souche.perf.db.interceptor.ShardingJdbcDataSourceInterceptor";
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
