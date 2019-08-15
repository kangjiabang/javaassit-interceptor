package com.souche.soucheagent.common.model;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/14 下午2:09
 * @Version: 1.0
 * @Description:  调用interceptor的beforeMethod执行的结果，如果需要忽略被拦截方法需要此类
 */
public class BeforeMethodResult {


    public BeforeMethodResult(boolean ignoreOriginExecute, Object result) {
        this.ignoreOriginExecute = ignoreOriginExecute;
        this.result = result;
    }

    /**
     *是否放弃执行拦截器拦截的方法，如果是的话，将返回Object结果
     */
    private boolean ignoreOriginExecute;

    /**
     * interceptor返回给用户的结果
     */
    private Object   result;


    public boolean ignoreOriginExecute() {
        return ignoreOriginExecute;
    }

    public Object getResult() {
        return result;
    }
}
