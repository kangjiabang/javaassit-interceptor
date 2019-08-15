package com.souche.perf.mock.handler;

public interface ResultHandler<T> {

    /**
     * 根据mock平台返回的data获取结果
     * @param data
     * @return
     */
    public T handle(String data);
}
