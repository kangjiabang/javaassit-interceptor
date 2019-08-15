package com.souche.soucheagent.common.exception;
/**
 * @Author: jiabangkang
 * @Date: 2019/6/24 下午4:26
 * @Version: 1.0
 * @Description: 
 */
public class AgentException extends   RuntimeException {



    public AgentException(Exception e) {
        super(e);
    }

    public AgentException(String message) {
        super(message);
    }

    public AgentException(String message, Exception e) {
        super(message,e);
    }

}
