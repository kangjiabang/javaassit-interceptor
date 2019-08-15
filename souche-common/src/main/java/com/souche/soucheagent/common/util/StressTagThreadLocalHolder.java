package com.souche.soucheagent.common.util;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
*
*  @Author: kangjiabang
*  @CreateTime: 2019/5/24 下午3:04
 * @version V1.0
*  @Description: 
*/
public class StressTagThreadLocalHolder {

    private static TransmittableThreadLocal<String> tagThreadLocal = new TransmittableThreadLocal();

    public static void setTagThreadLocal(String tag) {
        tagThreadLocal.set(tag);
    }

    public static String getTagThreadLocal() {
        return tagThreadLocal.get();
    }

    public static void removeThreadLocalTag() {
        tagThreadLocal.remove();
    }
}
