package com.souche.javaassitagent.define;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/11 上午9:10
 * @Version: 1.0
 * @Description: 
 */
public abstract class AbstractInterceptorDefine {


    /**
     * 拦截的类名
     * @return
     */
    public abstract String matchedClassName();

    /**
     * 拦截的方法名
     * @return
     */
    public abstract String methodName();

    /**
     * 拦截的方法参数
     * @return
     */
    public abstract String[] methodParams();

    /**
     * 拦截器名称
     * @return
     */
    public abstract String interceptor();
}
