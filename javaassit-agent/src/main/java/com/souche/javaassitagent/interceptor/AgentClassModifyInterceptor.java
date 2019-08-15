package com.souche.javaassitagent.interceptor;

import com.souche.soucheagent.common.model.BeforeMethodResult;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/11 上午9:15
 * @Version: 1.0
 * @Description: 
 */
@Slf4j
public abstract  class AgentClassModifyInterceptor {


    /**
     * 类加载之后的拦截
     * @param args
     * @param instance
     * @param clazz
     * @param methodName
     * @return Object
     */
    public abstract Object afterLoadedMethod(Object instance, Class clazz, String methodName, Object[] args);
    /**
     *
     ClassPool pool = ClassPool.getDefault();

     pool.appendClassPath(new LoaderClassPath(this.getClass().getClassLoader()));

     cc = pool.getAndRename("org.apache.http.impl.execchain.HttpResponseProxy","org.apache.http.impl.execchain.HttpResponseProxyNew");

     cc.setModifiers(Modifier.PUBLIC);

     // 添加无参构造器，以便可以调用newInstance
     CtConstructor constructor = new CtConstructor(null, cc);
     constructor.setModifiers(Modifier.PUBLIC);
     constructor.setBody("{}");
     cc.addConstructor(constructor);
     }
     */

    /**
     * 方法执行异常的拦截
     * @param args
     * @param instance
     * @param clazz
     * @param methodName
     */
    public abstract void handleException(Object instance,Class clazz,String methodName,Object[] args,Throwable e);
}
