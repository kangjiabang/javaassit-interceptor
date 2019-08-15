package com.souche.perf.db.interceptor;

import com.google.common.collect.Maps;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.jdbc.ConnectionImpl;
import com.mysql.cj.jdbc.JdbcConnection;
import com.souche.javaassitagent.interceptor.AgentMethodInterceptor;
import com.souche.soucheagent.common.model.BeforeMethodResult;
import com.souche.soucheagent.common.util.StressTagThreadLocalHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午3:48
 * @Version: 1.0
 * @Description:
 */
@Slf4j
public class ShardingJdbcDataSourceInterceptor_Deprecated extends AgentMethodInterceptor {


    public static final ShardingJdbcDataSourceInterceptor_Deprecated instance = new ShardingJdbcDataSourceInterceptor_Deprecated();


    private ConcurrentMap<SimpleHostInfo,JdbcConnection> shadowHostInfoToJdbcConnection = Maps.newConcurrentMap();

    //保存替换成影子连接之前正常的JdbcConnection
    private ThreadLocal<JdbcConnection> threadLocalConnection = new ThreadLocal<>();

    public ShardingJdbcDataSourceInterceptor_Deprecated() {
    }

    public static ShardingJdbcDataSourceInterceptor_Deprecated getInstance() {
        return instance;
    }



    /**
     * 方法执行之前的拦截
     *
     * @param args
     * @param instance
     * @param clazz
     * @param methodName
     */
    @Override
    public BeforeMethodResult beforeMethod(Object instance, Class clazz, String methodName, Object[] args) {

        //如果不带压测的标，直接返回
        if (StringUtils.isEmpty(StressTagThreadLocalHolder.getTagThreadLocal())) {
            return null;
        }
        long startTime = System.nanoTime();
        //将ClientPreparedStatement中JdbcConnection 替换成 影子库配置重新生成的JdbcConnection
        if (instance instanceof com.mysql.cj.jdbc.ClientPreparedStatement) {
            try {
                Field connField = clazz.getSuperclass().getDeclaredField("connection");
                connField.setAccessible(true);
                JdbcConnection  jdbcConnection = (JdbcConnection)connField.get(instance);
                //保存即将替换的jdbcConnection
                threadLocalConnection.set(jdbcConnection);

                Field hostInfoField = ConnectionImpl.class.getDeclaredField("origHostInfo");

                hostInfoField.setAccessible(true);
                HostInfo hostInfo = (HostInfo)hostInfoField.get(jdbcConnection);

                HostInfo shdowHostInfo = getShadowHostInfo(hostInfo);

                JdbcConnection shadowJdbcConnection = getShadowJdbcConnection(shdowHostInfo);

                //将ClientPreparedStatement中ConnectionImpl 替换成shadow的ConnectionImpl
                connField.set(instance,shadowJdbcConnection);

                log.info("convert connection from originHostInfo:{} to newHostInfo:{}.",hostInfo,shdowHostInfo);

            } catch (Exception e) {
                log.error("fail to execute ShardingJdbcDataSourceInterceptor_Deprecated beforeMethod",e);
                throw new RuntimeException("fail to execute ShardingJdbcDataSourceInterceptor_Deprecated beforeMethod" +
                        ExceptionUtils.getStackTrace(e));
            }
            log.info("before Method time spent: " + (System.nanoTime() - startTime)/1000000.0 + "ms");
        }


        return null;
    }

    private HostInfo getShadowHostInfo(HostInfo hostInfo) {

        ShadowDataSourceService shadowDataSourceService = ShadowDataSourceService.getInstance();


        String url = buildUrl(hostInfo);
        ShadowDataSourceService.DataSourceConfig dataSourceConfig = shadowDataSourceService.getShadowDataSourceByUrl(url);

        if (dataSourceConfig == null) {
            throw new RuntimeException("cannot get shadow datasource from apollo. please configure first, online url:" + url);
        }
        //to be replaceed by shadow host
        String host = dataSourceConfig.getHost();

        Map<String,String> maps = new HashMap<String,String>();
        //maps.put(PropertyKey.DBNAME.getKeyName(),hostInfo.getHostProperties().get(PropertyKey.DBNAME.getKeyName()) + Constants.SHADOW_SUFFIX);
        maps.put(PropertyKey.DBNAME.getKeyName(),dataSourceConfig.getDatabase());
        return new HostInfo(null,dataSourceConfig.getHost(),dataSourceConfig.getPort(),dataSourceConfig.getUsername(),dataSourceConfig.getPassword(),maps);
    }

    /**
     *  jdbc:mysql://mysql1.dev.scsite.net:3306/test
     * @param hostInfo
     * @return
     */
    private String buildUrl(HostInfo hostInfo) {

        return "jdbc:mysql://" + hostInfo.getHost() + ":" + hostInfo.getPort() + "/" + hostInfo.getDatabase();
    }

    /**
     * 根据shadowHostInfo获取影子库对应的的JdbcConnection
     * @param shdowHostInfo
     * @return
     * @throws SQLException
     */
    private JdbcConnection getShadowJdbcConnection(HostInfo shdowHostInfo) throws SQLException {

        //用户比较HostInfo
        SimpleHostInfo simpleHostInfo = new SimpleHostInfo(shdowHostInfo);
        //双重加锁保证线程安全
        if (shadowHostInfoToJdbcConnection.get(simpleHostInfo) == null) {
            synchronized (shadowHostInfoToJdbcConnection) {
                if (shadowHostInfoToJdbcConnection.get(simpleHostInfo) == null) {
                    JdbcConnection shadowJdbcConnection = ConnectionImpl.getInstance(shdowHostInfo);
                    shadowHostInfoToJdbcConnection.putIfAbsent(simpleHostInfo,shadowJdbcConnection);
                }
            }

        } else {
            log.info("get shadowJdbcConnection from cache,hostInfo:{}",simpleHostInfo);
        }
        return shadowHostInfoToJdbcConnection.get(simpleHostInfo);
    }


    /**
     * 方法执行之后的拦截
     *
     * @param args
     * @param instance
     * @param clazz
     * @param methodName
     * @return Object 返回值
     */
    @Override
    public Object afterMethod(Object instance, Class clazz, String methodName, Object[] args, Object result) {

        //如果不带压测的标，直接返回
        if (StringUtils.isEmpty(StressTagThreadLocalHolder.getTagThreadLocal())) {
            return result;
        }
        long startTime = System.nanoTime();
        try {
            if (threadLocalConnection.get() != null) {

                Field connField = clazz.getSuperclass().getDeclaredField("connection");
                connField.setAccessible(true);
                connField.set(instance,threadLocalConnection.get());
                //移除
                threadLocalConnection.remove();
            }

        } catch (Exception e) {
            log.error("fail to execute afterMethod of ShardingJdbcDataSourceInterceptor_Deprecated",e);
            throw new RuntimeException("fail to execute ShardingJdbcDataSourceInterceptor_Deprecated afterMethod" +
                    ExceptionUtils.getStackTrace(e));
        }
        log.info("after Method time spent: " + (System.nanoTime() - startTime)/1000000.0 + "ms");
        //执行方法之后，需要将jdbcConnection 恢复，需要调研是否需要修改
        return result;

    }

    @Override
    public void handleException(Object instance, Class clazz, String methodName, Object[] args, Throwable e) {
        log.error("fail to execute AlibabaDubboConsumeInterceptor interceptor.", e);
    }


    /**
     * 该HostInfo 主要用来保存简单的 HostInfo信息用于缓存比较Host是否存在使用
     */
    private class SimpleHostInfo {

        private String host;
        private int port;
        private String user;
        private String password;
        private Map<String, String> hostProperties = null;

        public SimpleHostInfo(HostInfo hostInfo) {
            this.host = hostInfo.getHost();
            this.port = hostInfo.getPort();
            this.user = hostInfo.getUser();
            this.password = hostInfo.getPassword();
            this.hostProperties = hostInfo.getHostProperties();
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleHostInfo that = (SimpleHostInfo) o;
            return port == that.port &&
                    Objects.equals(host, that.host) &&
                    Objects.equals(user, that.user) &&
                    Objects.equals(password, that.password) &&
                    Objects.equals(hostProperties, that.hostProperties);
        }

        @Override
        public int hashCode() {

            return Objects.hash(host, port, user, password, hostProperties);
        }

        @Override
        public String toString() {
            return "SimpleHostInfo{" +
                    "host='" + host + '\'' +
                    ", port=" + port +
                    ", user='" + user + '\'' +
                    ", password='" + password + '\'' +
                    ", hostProperties=" + hostProperties +
                    '}';
        }
    }
}
