package com.souche.javaassitagent.classloader;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
/**
 * @Author: jiabangkang
 * @Date: 2019/6/11 上午9:10
 * @Version: 1.0
 * @Description: 
 */

@Slf4j
public class AgentClassLoader extends ClassLoader {


    private JarFile jarFile;

    public AgentClassLoader(JarFile jarFile,ClassLoader classLoader) {
        super(classLoader);
        this.jarFile = jarFile;
    }
    @Override
    protected Class<?> findClass(String name) {

        try {
            String className = ((String) name).replace('.', '/') + ".class";
            //读取services-plugin.properties文件
            InputStream inputStream = jarFile.getInputStream((jarFile.getJarEntry(className)));

            byte[] classBytes = input2byte(inputStream);

            return defineClass(name,classBytes,0,classBytes.length);

        } catch (Exception e) {
            log.error("fail to find class[{}]",name,e);
        }
        return null;
    }

    public static final byte[] input2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }
}
