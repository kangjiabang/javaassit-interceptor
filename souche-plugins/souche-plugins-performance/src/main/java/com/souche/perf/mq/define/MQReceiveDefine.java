package com.souche.perf.mq.define;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;

public class MQReceiveDefine extends AbstractInterceptorDefine {


    private String className = "com.aliyun.openservices.ons.api.impl.rocketmq.ConsumerImpl$MessageListenerImpl";
    private String methodName = "consumeMessage";

    private String intercetor = "com.souche.perf.mq.interceptor.MQReceiveInterceptor";
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
        return new String[] {"java.util.List","com.aliyun.openservices.shade.com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext"};
    }
}
