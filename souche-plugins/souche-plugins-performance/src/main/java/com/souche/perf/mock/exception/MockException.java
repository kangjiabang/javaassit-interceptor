package com.souche.perf.mock.exception;
/**
 * @Author: jiabangkang
 * @Date: 2019/6/24 下午4:26
 * @Version: 1.0
 * @Description: 
 */
public class MockException extends   RuntimeException {



    public MockException(Exception e) {
        super(e);
    }

    public MockException(String message) {
        super(message);
    }

    public MockException(String message,Exception e) {
        super(message,e);
    }

}
