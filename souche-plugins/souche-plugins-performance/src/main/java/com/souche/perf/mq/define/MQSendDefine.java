package com.souche.perf.mq.define;

import com.souche.javaassitagent.define.AbstractInterceptorDefine;

public class MQSendDefine extends AbstractInterceptorDefine {


    private String className = "com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.DefaultMQProducer";
    private String methodName = "send";

    private String intercetor = "com.souche.perf.mq.interceptor.MQSendInterceptor";
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
        return new String[] {"com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.Message"};
    }

}
