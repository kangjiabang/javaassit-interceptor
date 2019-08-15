package com.souche.soucheagent.common.exception;


/**
 * @Author: jiabangkang
 * @Date: 2019/6/11 上午9:08
 * @Version: 1.0
 * @Description: 
 */
public class IgnoreOriginExecuteException extends RuntimeException {

    private Object returnResult;

    public Object getReturnResult () {
        return returnResult;
    }

    public IgnoreOriginExecuteException(Object returnResult, String message) {
        this(message);
        this.returnResult = returnResult;
    }

    public IgnoreOriginExecuteException(Object returnResult, String message, Throwable cause) {
        super(message, cause);
        this.returnResult = returnResult;
    }

    public IgnoreOriginExecuteException(Object returnResult, Throwable cause) {
        super(cause);
        this.returnResult = returnResult;
    }

    public IgnoreOriginExecuteException(Object returnResult, String message, Throwable cause,
                                        boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.returnResult = returnResult;
    }

    public IgnoreOriginExecuteException() {
    }

    public IgnoreOriginExecuteException(String message) {
        super(message);
    }

    public IgnoreOriginExecuteException(String message, Throwable cause) {
        super(message, cause);
    }

    public IgnoreOriginExecuteException(Throwable cause) {
        super(cause);
    }

    public IgnoreOriginExecuteException(String message, Throwable cause,
                                        boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
