package com.souche.javaassitagent.helper;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/11 上午9:10
 * @Version: 1.0
 * @Description: 
 */
@Slf4j
public class JarFinder {


    /**
     * 加载service.properties 文件
     */
    public static List<JarFile> loadAllJars(ClassLoader classLoader) {

        List<JarFile> jarFileList = Lists.newArrayList();
        try {

            File path = new File(getPluginsPath());
            File[] files = path.listFiles();

            if (ArrayUtils.isNotEmpty(files)) {
                for (File file : files) {
                    if (file.getName().endsWith(".jar")) {
                        jarFileList.add(new JarFile(file));
                    }
                }
            }

            return jarFileList;

        }  catch (Throwable e) {
            log.error("fail to load services.properties.", e);
        }
        return jarFileList;
    }

    /**
     * 获取插件路径
     * @return
     */
    public static String getPluginsPath() {

        try {
            String jarFullName = JarFinder.class.getProtectionDomain().getCodeSource().getLocation().getPath();

            String decodedPath = URLDecoder.decode(jarFullName, "UTF-8");
            String jarPath = jarFullName.substring(0, decodedPath.lastIndexOf(File.separator));
            return jarPath + "/../plugins";
        } catch (UnsupportedEncodingException e) {
            log.error("fail to decode jarFullName.",e);
        }
        return "";
    }

}
