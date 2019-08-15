package com.souche.javaassitagent;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;
import com.souche.javaassitagent.helper.JarFinder;
import com.souche.javaassitagent.helper.ServicePluginsBoot;
import com.souche.javaassitagent.interceptor.AgentMethodInterceptor;
import javassist.*;
import javassist.runtime.Desc;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/11 上午9:09
 * @Version: 1.0
 * @Description:
 */
@Slf4j
public class StressJavaAssitTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        CtClass clazz = null;
        try {

            if (StringUtils.isNotEmpty(className)) {

                className = className.replace('/', '.');

                //如果包含，开始进行拦截操作
                if (ServicePluginsBoot.contains(className)) {

                    log.info("Transforming class " + className);
                    ClassPool classPool = new ClassPool(true);
                    //将plugins的路径加入到classPool中，暂时无用
                    classPool.appendClassPath(JarFinder.getPluginsPath() + "/*");
                    classPool.importPackage("com.souche.soucheagent.common.model");
                    //默认Desc使用上下文的类加载器，用户$class加载时不报错
                    Desc.useContextClassLoader = true;

                    clazz = getCtClass(classPool, classfileBuffer, loader);

                    if (loader instanceof URLClassLoader) {

                        URLClassLoader urlClassLoader = (URLClassLoader) loader;

                        Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{java.net.URL.class});
                        addUrl.setAccessible(true);

                        List<JarFile> jarFileList = JarFinder.loadAllJars(StressJavaAssitTransformer.class.getClassLoader());
                        //反射将url添加到路径中
                        if (CollectionUtils.isNotEmpty(jarFileList)) {
                            for (JarFile jarFile : jarFileList) {
                                URL url = new URL("jar:file:" + jarFile.getName() + "!/");


                                addUrl.invoke(urlClassLoader, url);
                            }
                        }


                        List<AbstractInterceptorDefine> interceptorDefines = ServicePluginsBoot.getAbstractInterceptorDefine(className);

                        if (CollectionUtils.isNotEmpty(interceptorDefines)) {

                            for (int i = 0; i < interceptorDefines.size(); i++) {

                                //AgentMethodInterceptor methodInterceptor = null;//(AgentMethodInterceptor)loader.loadClass(interceptorDefine.interceptor()).newInstance();

                                updateMethod(classPool, clazz, interceptorDefines.get(i));
                            }
                        }

                        return clazz.toBytecode();
                    }

                }
            }
        } catch (Throwable e) {
            log.error("fail transform className[{}]", className, e);
        }
        //no transform at all
        return null;
    }

    /**
     * @param clazz
     * @param interceptorDefine
     */
    private void updateMethod(ClassPool classPool, CtClass clazz,AbstractInterceptorDefine interceptorDefine) throws NotFoundException, CannotCompileException {

        CtMethod ctMethod = null;
        if (ArrayUtils.isNotEmpty(interceptorDefine.methodParams())) {
            CtClass[] ctClasses = new CtClass[interceptorDefine.methodParams().length];

            for (int i = 0; i < interceptorDefine.methodParams().length; i++) {
                ctClasses[i] = classPool.get(interceptorDefine.methodParams()[i]);
            }

            ctMethod = clazz.getDeclaredMethod(interceptorDefine.methodName(), ctClasses);

        } else {

            ctMethod = clazz.getDeclaredMethod(interceptorDefine.methodName());

        }

        CtClass beforeMethodResultClass = classPool.get("com.souche.soucheagent.common.model.BeforeMethodResult");
        ctMethod.addLocalVariable("beforeMethodResult",beforeMethodResultClass);
        StringBuilder insertBeforeBuilder = new StringBuilder();
        insertBeforeBuilder.append(" beforeMethodResult = ");
        insertBeforeBuilder.append(interceptorDefine.interceptor() + ".getInstance()");
        insertBeforeBuilder.append(String.format(".beforeMethod($0,$class,\"%s\",$args);", interceptorDefine.methodName()));
        insertBeforeBuilder.append(" if (beforeMethodResult != null && beforeMethodResult.ignoreOriginExecute()) {");
        insertBeforeBuilder.append(" return (beforeMethodResult.getResult());");

        insertBeforeBuilder.append("}");
        log.info(insertBeforeBuilder.toString());
        ctMethod.insertBefore(insertBeforeBuilder.toString());

        StringBuilder insertAfterBuilder = new StringBuilder();
        insertAfterBuilder.append("return ($r)");
        insertAfterBuilder.append(interceptorDefine.interceptor() + ".getInstance()");
        insertAfterBuilder.append(String.format(".afterMethod($0,$class,\"%s\",$args,$_);", interceptorDefine.methodName()));
        log.info(insertAfterBuilder.toString());
        ctMethod.insertAfter(insertAfterBuilder.toString());


        StringBuilder dealExceptionBuilder = new StringBuilder();
        dealExceptionBuilder.append(interceptorDefine.interceptor() + ".getInstance()");
        dealExceptionBuilder.append(String.format(".handleException($0,$class,\"%s\",$args,$e);", interceptorDefine.methodName()));
        log.info(dealExceptionBuilder.toString());

        CtClass etype = ClassPool.getDefault().get("java.lang.Exception");
        ctMethod.addCatch("{  " + dealExceptionBuilder.toString() + " throw $e;}", etype);
        //ctMethod.addCatch("{  if ($e instanceof com.souche.soucheagent.common.exception.IgnoreOriginExecuteException) { return ($r)((com.souche.soucheagent.common.exception.IgnoreOriginExecuteException)$e).getReturnResult();} " + dealExceptionBuilder.toString() + " throw $e;}", etype);


    }

    /**
     * @param classfileBuffer
     * @param classLoader
     * @return
     * @throws IOException
     */
    private static CtClass getCtClass(ClassPool classPool, byte[] classfileBuffer, ClassLoader classLoader) throws IOException {


        if (classLoader == null) {
            classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
        } else {
            classPool.appendClassPath(new LoaderClassPath(classLoader));
        }

        CtClass clazz = classPool.makeClass(new ByteArrayInputStream(classfileBuffer), false);
        clazz.defrost();
        return clazz;

    }
}
