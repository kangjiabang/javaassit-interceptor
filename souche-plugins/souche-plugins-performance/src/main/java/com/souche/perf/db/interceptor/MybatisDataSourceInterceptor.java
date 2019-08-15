package com.souche.perf.db.interceptor;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Maps;
import com.souche.soucheagent.common.exception.AgentException;
import com.souche.soucheagent.common.model.BeforeMethodResult;
import com.souche.soucheagent.common.util.StressTagThreadLocalHolder;
import com.souche.javaassitagent.interceptor.AgentMethodInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.datasource.pooled.PooledDataSource;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午3:48
 * @Version: 1.0
 * @Description:
 */
@Slf4j
public class MybatisDataSourceInterceptor extends AgentMethodInterceptor {


    public static final MybatisDataSourceInterceptor instance = new MybatisDataSourceInterceptor();


    public MybatisDataSourceInterceptor() {
    }

    public static MybatisDataSourceInterceptor getInstance() {
        return instance;
    }


    private ConcurrentMap<String, DataSource> urlToShadowDataSources = Maps.newConcurrentMap();

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
        return null;
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

        try {
            //如果不带压测的标，直接返回
            if (StringUtils.isEmpty(StressTagThreadLocalHolder.getTagThreadLocal())) {
                return result;
            }

            if (result instanceof PooledDataSource) {

                PooledDataSource oldPooledDataSource = (PooledDataSource) result;
                //如果url对应的影子库的dataSource不为空
                if (urlToShadowDataSources.get(oldPooledDataSource.getUrl()) != null) {
                    return urlToShadowDataSources.get(oldPooledDataSource.getUrl());
                }
                PooledDataSource newPooledDataSource = getPooledDataSource(oldPooledDataSource);
                urlToShadowDataSources.putIfAbsent(oldPooledDataSource.getUrl(),newPooledDataSource);

                log.info("pooled datasource url: " + newPooledDataSource.getUrl());
                return newPooledDataSource;

            } else if (result instanceof DruidDataSource) {
                DruidDataSource oldDruidDataSource = (DruidDataSource) (result);

                //如果url对应的影子库的dataSource不为空
                if (urlToShadowDataSources.get(oldDruidDataSource.getUrl()) != null) {
                    return urlToShadowDataSources.get(oldDruidDataSource.getUrl());
                }
                DruidDataSource newDruidDataSource = getDruidDataSource(oldDruidDataSource);
                urlToShadowDataSources.putIfAbsent(oldDruidDataSource.getUrl(),newDruidDataSource);


                log.info(" druid datasource url: " + newDruidDataSource.getUrl());
                return newDruidDataSource;

            }
            log.info("origin datasource:" + result);
            return result;
        } catch (Exception e) {
            throw new AgentException("fail to execute MybatisDataSourceInterceptor beforeMethod." + ExceptionUtils.getStackTrace(e));
        }
    }


    public PooledDataSource getPooledDataSource(PooledDataSource oldPooledDataSource) {
        PooledDataSource pooledDataSource = new PooledDataSource();
        pooledDataSource.setDriver(oldPooledDataSource.getDriver());

        ShadowDataSourceService.DataSourceConfig shadowDataSourceConfig = ShadowDataSourceService.
                getInstance().getShadowDataSourceByUrl(oldPooledDataSource.getUrl());

        if (shadowDataSourceConfig == null) {
            throw new IllegalArgumentException("shadow datasources not configured or wrong,please check apollo ${stress.datasource.onlineToshdaow.mapping} configuration. online url: " + oldPooledDataSource.getUrl());
        }

        pooledDataSource.setUrl(shadowDataSourceConfig.getUrl());
        pooledDataSource.setUsername(shadowDataSourceConfig.getUsername());
        pooledDataSource.setPassword(shadowDataSourceConfig.getPassword());

        pooledDataSource.setPoolMaximumActiveConnections(oldPooledDataSource.getPoolMaximumActiveConnections());
        pooledDataSource.setLoginTimeout(oldPooledDataSource.getLoginTimeout());
        pooledDataSource.setPoolMaximumIdleConnections(oldPooledDataSource.getPoolMaximumIdleConnections());
        pooledDataSource.setLoginTimeout(oldPooledDataSource.getLoginTimeout());
        pooledDataSource.setPoolMaximumCheckoutTime(oldPooledDataSource.getPoolMaximumCheckoutTime());
        pooledDataSource.setPoolTimeToWait(oldPooledDataSource.getPoolTimeToWait());

        return pooledDataSource;
    }

    public DruidDataSource getDruidDataSource(DruidDataSource oldDruidDataSource) {
        DruidDataSource druidDataSource = new DruidDataSource();
        //druidDataSource.setAsyncInit(oldDruidDataSource.isAsyncInit());
        druidDataSource.setConnectProperties(oldDruidDataSource.getConnectProperties());
        druidDataSource.setEnable(oldDruidDataSource.isEnable());
        //druidDataSource.setKeepAlive(oldDruidDataSource.isKeepAlive());
        //druidDataSource.setKillWhenSocketReadTimeout(oldDruidDataSource.isKillWhenSocketReadTimeout());
        druidDataSource.setMaxActive(oldDruidDataSource.getMaxActive());
        druidDataSource.setPoolPreparedStatements(oldDruidDataSource.isPoolPreparedStatements());
        druidDataSource.setDbType(oldDruidDataSource.getDbType());
        druidDataSource.setDriver(oldDruidDataSource.getDriver());
        druidDataSource.setName(oldDruidDataSource.getName());


        ShadowDataSourceService.DataSourceConfig shadowDataSourceConfig = ShadowDataSourceService.
                getInstance().getShadowDataSourceByUrl(oldDruidDataSource.getUrl());

        if (shadowDataSourceConfig == null) {
            throw new IllegalArgumentException("shadow datasources not configured or wrong,please check apollo ${stress.datasource.onlineToshdaow.mapping} configuration. online url: " + oldDruidDataSource.getUrl());
        }
        druidDataSource.setUrl(shadowDataSourceConfig.getUrl());
        druidDataSource.setUsername(shadowDataSourceConfig.getUsername());
        druidDataSource.setPassword(shadowDataSourceConfig.getPassword());

        //druidDataSource.setFilters(oldDruidDataSource.getF);
        druidDataSource.setLogAbandoned(oldDruidDataSource.isLogAbandoned());
        druidDataSource.setTestOnBorrow(oldDruidDataSource.isTestOnBorrow());
        druidDataSource.setTestOnReturn(oldDruidDataSource.isTestOnReturn());
        druidDataSource.setTestWhileIdle(oldDruidDataSource.isTestWhileIdle());
        druidDataSource.setTimeBetweenConnectErrorMillis(oldDruidDataSource.getTimeBetweenConnectErrorMillis());
        druidDataSource.setMinEvictableIdleTimeMillis(oldDruidDataSource.getMinEvictableIdleTimeMillis());
        druidDataSource.setRemoveAbandoned(oldDruidDataSource.isRemoveAbandoned());
        druidDataSource.setRemoveAbandonedTimeoutMillis(oldDruidDataSource.getRemoveAbandonedTimeoutMillis());
        druidDataSource.setValidationQuery(oldDruidDataSource.getValidationQuery());

        return druidDataSource;
    }

    @Override
    public void handleException(Object instance, Class clazz, String methodName, Object[] args, Throwable e) {
        log.error("fail to execute AlibabaDubboConsumeInterceptor interceptor.", e);

    }
}
