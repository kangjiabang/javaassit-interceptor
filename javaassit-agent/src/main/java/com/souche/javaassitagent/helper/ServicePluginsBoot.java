package com.souche.javaassitagent.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.souche.javaassitagent.classloader.AgentClassLoader;
import com.souche.javaassitagent.define.AbstractInterceptorDefine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarFile;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/11 上午9:10
 * @Version: 1.0
 * @Description: 
 */
@Slf4j
public class ServicePluginsBoot {

    private  static List<AbstractInterceptorDefine> interceptorDefineList = Lists.newArrayList();

    private  static List<String> classNames = Lists.newArrayList();

    private  static ConcurrentMap<String,List<AbstractInterceptorDefine>> classNameToInterceptorDefines
            = Maps.newConcurrentMap();

    static {
        loadDefines(ClassLoader.getSystemClassLoader());
    }
    /**
     * 加载service.properties 文件
     */
    private static void loadDefines(ClassLoader classLoader) {
        try {

            List<JarFile> jarFileList = JarFinder.loadAllJars(ServicePluginsBoot.class.getClassLoader());

            //遍历jar文件
            for (JarFile jarFile : jarFileList) {
                Properties properties = new Properties();


                //读取services-plugin.properties文件
                InputStream inputStream = jarFile.getInputStream((jarFile.getJarEntry("services-plugin.properties")));

                properties.load(inputStream);

                Collection<Object> defineListStrings = properties.values();


                loadDefines(classLoader, defineListStrings, jarFile);

            }
        }  catch (ClassNotFoundException e) {
            log.error("fail to load class.",e);
        }
        catch (Throwable e) {
            log.error("fail to load services.properties.",e);
        }
    }

    /**
     * 加载class
     *
     * @param classLoader
     * @param defineListStrings
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    private static void loadDefines(ClassLoader classLoader, Collection<Object> defineListStrings, JarFile jarFile) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
        if (defineListStrings != null && defineListStrings.size() != 0) {

            for (Object define : defineListStrings) {

                AgentClassLoader agentClassLoader = new AgentClassLoader(jarFile,classLoader);
                AbstractInterceptorDefine interceptorDefine = (AbstractInterceptorDefine) agentClassLoader.loadClass((String) define).newInstance();

                synchronized (classNameToInterceptorDefines) {

                    List<AbstractInterceptorDefine> interceptorDefines =
                            CollectionUtils.isEmpty(classNameToInterceptorDefines.get(interceptorDefine.matchedClassName())) ?
                                    Lists.newArrayList() : classNameToInterceptorDefines.get(interceptorDefine.matchedClassName());
                    interceptorDefines.add(interceptorDefine);

                    classNameToInterceptorDefines.put(interceptorDefine.matchedClassName(), interceptorDefines);
                    interceptorDefineList.add(interceptorDefine);
                }
            }
        }
    }

    public static List<AbstractInterceptorDefine> getInterceptorDefineList() {
        return interceptorDefineList;
    }

    public static List<String> getClassNames() {
        return classNames;
    }


    public static List<AbstractInterceptorDefine> getAbstractInterceptorDefine(String className) {
        return classNameToInterceptorDefines.get(className);
    }
    public static boolean contains(String className) {
        return MapUtils.isNotEmpty(classNameToInterceptorDefines) &&
                classNameToInterceptorDefines.containsKey(className);
    }
}
