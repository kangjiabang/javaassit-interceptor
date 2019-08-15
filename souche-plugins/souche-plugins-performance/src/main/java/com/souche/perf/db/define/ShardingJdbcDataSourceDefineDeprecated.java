package com.souche.perf.db.define;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午4:15
 * @Version: 1.0
 * @Description: 
 */
public class ShardingJdbcDataSourceDefineDeprecated extends AbstractInterceptorDefine {


    private String className = "com.mysql.cj.jdbc.ClientPreparedStatement_Deprecated";
    private String methodName = "executeInternal";

    private String intercetor = "com.souche.perf.db.interceptor.ShardingJdbcDataSourceInterceptor_Deprecated";
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
        return new String[]{
                "int","com.mysql.cj.protocol.Message","boolean","boolean"
                ,"com.mysql.cj.protocol.ColumnDefinition","boolean"
        };
    }

}
