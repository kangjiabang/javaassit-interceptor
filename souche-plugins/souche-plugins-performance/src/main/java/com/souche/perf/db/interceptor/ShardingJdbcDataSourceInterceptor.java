package com.souche.perf.db.interceptor;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.souche.javaassitagent.interceptor.AgentMethodInterceptor;
import com.souche.soucheagent.common.exception.AgentException;
import com.souche.soucheagent.common.model.BeforeMethodResult;
import com.souche.soucheagent.common.util.StressTagThreadLocalHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/12 下午3:48
 * @Version: 1.0
 * @Description:
 */
@Slf4j
public class ShardingJdbcDataSourceInterceptor extends AgentMethodInterceptor {


    public static final ShardingJdbcDataSourceInterceptor instance = new ShardingJdbcDataSourceInterceptor();


    private ShardingDataSource shadowShardingDataSource = null;

    public ShardingJdbcDataSourceInterceptor() {
    }

    public static ShardingJdbcDataSourceInterceptor getInstance() {
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

        try {
            //如果带压测的标，从shadowShardingDatasource获取连接，返回
            if (StringUtils.isNotEmpty(StressTagThreadLocalHolder.getTagThreadLocal())) {

                if (shadowShardingDataSource != null) {
                    ShardingConnection shardingConnection = new ShardingConnection(shadowShardingDataSource.getDataSourceMap(),
                            shadowShardingDataSource.getShardingContext(),shadowShardingDataSource.getShardingTransactionManagerEngine(), TransactionTypeHolder.get());

                    String shadowDatabase = getDatabaseInfo(shadowShardingDataSource);
                    log.info("get sharding connection from shadowShardingDatasource.urls:{}",shadowDatabase);
                    return new BeforeMethodResult(true,shardingConnection);
                }
            }

            return null;
        } catch (Exception e) {
            throw new AgentException("fail to execute ShardingJdbcDataSourceInterceptor beforeMethod. " +
                    ExceptionUtils.getStackTrace(e));
        }
    }

    private String getDatabaseInfo(ShardingDataSource shadowShardingDataSource) {
        Map<String,DataSource> maps = shadowShardingDataSource.getDataSourceMap();
        StringBuilder urls = new StringBuilder();
        urls.append("[");
        maps.forEach( (key,value) -> {

            if (value instanceof org.apache.commons.dbcp2.BasicDataSource) {
                org.apache.commons.dbcp2.BasicDataSource basicDataSource = (org.apache.commons.dbcp2.BasicDataSource) value;
                urls.append(basicDataSource.getUrl()  + "," );
            } else if (value instanceof org.apache.commons.dbcp.BasicDataSource) {
                org.apache.commons.dbcp.BasicDataSource basicDataSource = (org.apache.commons.dbcp.BasicDataSource) value;
                urls.append(basicDataSource.getUrl()  + "," );
            }
        });
        return urls.substring(0,urls.length() - 1) + "]";
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
        try {

            //如果不为空，说明已经在beforeMethod初始化了，直接返回result
            if (shadowShardingDataSource != null) {
                return result;
            }

            if (result instanceof org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection) {

                //初始化shadowDataSource
                initShadowShardingDataSource((ShardingConnection) result);

                return new ShardingConnection(shadowShardingDataSource.getDataSourceMap(),shadowShardingDataSource.getShardingContext(),shadowShardingDataSource.getShardingTransactionManagerEngine(), TransactionTypeHolder.get());

            }

        } catch (Exception e) {
            log.error("fail to execute afterMethod of ShardingJdbcDataSourceInterceptor",e);
            throw new AgentException("fail to execute ShardingJdbcDataSourceInterceptor afterMethod. " +
                    ExceptionUtils.getStackTrace(e));
        }
        return result;

    }

    /**
     * 初始化dataSource
     * @param result
     * @throws SQLException
     */
    private void initShadowShardingDataSource(ShardingConnection result) throws SQLException {
        if (shadowShardingDataSource == null) {

            synchronized (this) {

                if (shadowShardingDataSource == null) {

                    ShardingConnection shardingConnection = result;

                    Map<String, DataSource> dataSourceMap = getShadowDataSourceMap(shardingConnection);

                    shadowShardingDataSource = new ShardingDataSource(dataSourceMap,
                            shardingConnection.getShardingContext().getShardingRule(),
                            shardingConnection.getShardingContext().getShardingProperties().getProps());
                }

            }
        }
    }

    private Map<String, DataSource> getShadowDataSourceMap(ShardingConnection shardingConnection) {
        try {
            Map<String, DataSource> originDataSourceMap = shardingConnection.getDataSourceMap();

            Gson gson = new Gson();
            Map<String,DataSource> shadowDataSourceMap = Maps.newHashMap();

            if (MapUtils.isNotEmpty(originDataSourceMap)) {

                Set<Map.Entry<String,DataSource>> elements = originDataSourceMap.entrySet();

                for (Map.Entry<String,DataSource> element : elements) {

                    if (element.getValue() instanceof  org.apache.commons.dbcp2.BasicDataSource) {

                        org.apache.commons.dbcp2.BasicDataSource shadowBasicDataSource = new org.apache.commons.dbcp2.BasicDataSource();

                        //将datasource 的url替换成 影子表的url
                        modifyDataSourceAttribute(shadowBasicDataSource,(org.apache.commons.dbcp2.BasicDataSource)element.getValue());

                        shadowDataSourceMap.put(element.getKey(),shadowBasicDataSource);
                    } else if (element.getValue() instanceof  org.apache.commons.dbcp.BasicDataSource) {

                        org.apache.commons.dbcp.BasicDataSource shadowBasicDataSource = new org.apache.commons.dbcp.BasicDataSource();

                        //将datasource 的url替换成 影子表的url
                        modifyDataSourceAttribute(shadowBasicDataSource,(org.apache.commons.dbcp.BasicDataSource)element.getValue());

                        shadowDataSourceMap.put(element.getKey(),shadowBasicDataSource);
                    }
                }
            }
            return shadowDataSourceMap;
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("fail to get shadowDataSource map." + ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 修改dataSource的属性
     * @param dataSource
     */
    private void modifyDataSourceAttribute(DataSource dataSource,DataSource originDataSource) {

        ShadowDataSourceService shadowDataSourceService = ShadowDataSourceService.getInstance();
        if (dataSource instanceof org.apache.commons.dbcp2.BasicDataSource) {
            org.apache.commons.dbcp2.BasicDataSource basicDataSource2 =
                    (org.apache.commons.dbcp2.BasicDataSource)dataSource;

            org.apache.commons.dbcp2.BasicDataSource basicOriginDataSource2 =
                    (org.apache.commons.dbcp2.BasicDataSource)originDataSource;

            ShadowDataSourceService.DataSourceConfig shadowDataSourceConfig = shadowDataSourceService.getShadowDataSourceByUrl(basicOriginDataSource2.getUrl());

            if (shadowDataSourceConfig == null) {
                throw new RuntimeException("cannot get shadow datasource from apollo. please configure first, online url:" + basicDataSource2.getUrl());
            }
            basicDataSource2.setDriverClassName(basicOriginDataSource2.getDriverClassName());
            basicDataSource2.setUrl(shadowDataSourceConfig.getUrl());
            basicDataSource2.setUsername(shadowDataSourceConfig.getUsername());
            basicDataSource2.setPassword(shadowDataSourceConfig.getPassword());
            basicDataSource2.setMaxTotal(basicOriginDataSource2.getMaxTotal());
            basicDataSource2.setMaxIdle(basicOriginDataSource2.getMaxIdle());
            basicDataSource2.setMinIdle(basicOriginDataSource2.getMinIdle());
            basicDataSource2.setMaxOpenPreparedStatements(basicOriginDataSource2.getMaxOpenPreparedStatements());
            basicDataSource2.setDefaultQueryTimeout(basicOriginDataSource2.getDefaultQueryTimeout());

            // TODO other attributes set
        } else if (dataSource instanceof org.apache.commons.dbcp.BasicDataSource) {
            org.apache.commons.dbcp.BasicDataSource basicDataSource =
                    (org.apache.commons.dbcp.BasicDataSource)dataSource;

            org.apache.commons.dbcp.BasicDataSource basicOriginDataSource =
                    (org.apache.commons.dbcp.BasicDataSource)originDataSource;

            ShadowDataSourceService.DataSourceConfig shadowDataSourceConfig = shadowDataSourceService.getShadowDataSourceByUrl(basicOriginDataSource.getUrl());

            if (shadowDataSourceConfig == null) {
                throw new RuntimeException("cannot get shadow datasource from apollo. please configure first, online url:" + basicDataSource.getUrl());
            }
            basicDataSource.setDriverClassName(basicOriginDataSource.getDriverClassName());
            basicDataSource.setUrl(shadowDataSourceConfig.getUrl());
            basicDataSource.setUsername(shadowDataSourceConfig.getUsername());
            basicDataSource.setPassword(shadowDataSourceConfig.getPassword());
            basicDataSource.setMaxIdle(basicOriginDataSource.getMaxIdle());
            basicDataSource.setMinIdle(basicOriginDataSource.getMinIdle());
            basicDataSource.setMaxOpenPreparedStatements(basicOriginDataSource.getMaxOpenPreparedStatements());

            // TODO other attributes set
        }
        //TODO  other datasource

    }

    @Override
    public void handleException(Object instance, Class clazz, String methodName, Object[] args, Throwable e) {
        log.error("fail to execute ShardingJdbcDataSourceInterceptor interceptor.", e);
    }

}
