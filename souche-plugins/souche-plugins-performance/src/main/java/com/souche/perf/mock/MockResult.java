package com.souche.perf.mock;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/24 下午4:18
 * @Version: 1.0
 * @Description:  mock 平台返回的结果
 */
public class MockResult<T> {

    private long spentTimeInMill;

    /**
     * http 请求返回的响应body
     */
    private String data;


    private  T   response;


    public MockResult(long spentTimeInMill, String data,T response) {
        this.spentTimeInMill = spentTimeInMill;
        this.data = data;
        this.response = response;
    }


    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    public long getSpentTimeInMill() {
        return spentTimeInMill;
    }

    public void setSpentTimeInMill(long spentTimeInMill) {
        this.spentTimeInMill = spentTimeInMill;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MockResult{" +
                "spentTimeInMill=" + spentTimeInMill +
                ", data=" + data +
                '}';
    }
}
