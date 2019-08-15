package com.souche.javaassitagent;

import javassist.CtClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/11 上午9:17
 * @Version: 1.0
 * @Description: 
 */
@Slf4j
public class StressJavaAssitAgent {

    static void install(String agentArgs, Instrumentation inst) {
        log.info("[StressJavaAssitAgent.install] agentArgs: " + agentArgs + ", Instrumentation: " + inst);
        ClassFileTransformer transformer = new StressJavaAssitTransformer();
        inst.addTransformer(transformer, true);
        CtClass.debugDump = "./dump";
        log.info("[StressJavaAssitAgent.install] addTransformer success.");
    }

    public static void premain(String arguments, Instrumentation instrumentation) {

        install(arguments, instrumentation);
    }


}
