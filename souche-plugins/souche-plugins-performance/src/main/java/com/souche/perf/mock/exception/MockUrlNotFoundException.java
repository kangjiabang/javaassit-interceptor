package com.souche.perf.mock.exception;
/**
 * @Author: jiabangkang
 * @Date: 2019/6/24 下午4:26
 * @Version: 1.0
 * @Description: 
 */
public class MockUrlNotFoundException extends   RuntimeException {



    public MockUrlNotFoundException(Exception e) {
        super(e);
    }

    public MockUrlNotFoundException(String message) {
        super(message);
    }

    public MockUrlNotFoundException(String message, Exception e) {
        super(message,e);
    }

}
