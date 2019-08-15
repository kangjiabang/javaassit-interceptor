package com.souche.perf.db.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ctrip.framework.apollo.Apollo;
import com.google.gson.JsonObject;
import com.souche.optimus.common.config.OptimusConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: jiabangkang
 * @Date: 2019/6/18 上午9:58
 * @Version: 1.0
 * @Description: 获取影子库和线上库的服务
 */
@Slf4j
public class ShadowDataSourceService {

    //匹配数据库的URL，jdbc:mysql://mysql1.dev.scsite.net:3306/test
    public static Pattern pattern = Pattern.compile(".*?//(.*?):(\\d+)/(.*?)");

    public static final String DATASOURCE_URL_PREFIX = "jdbc";
    public static final String ONLINEDATA_SOURCE = "onlinedataSource";
    public static final String SHADOWDATA_SOURCE = "shadowdataSource";
    public static final String URL = "url";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    private Map<DataSourceConfig, DataSourceConfig> dataSourceConfigToShadowConfig = new ConcurrentHashMap<>();
    private Map<String, DataSourceConfig> urlDataSourceConfigToShadowConfig = new ConcurrentHashMap<>();

    private static final String STRESS_DATASOURCE_MAPPING = "stress.datasource.onlineToshdaow.mapping";

    private static final ShadowDataSourceService instance = new ShadowDataSourceService();

    public static ShadowDataSourceService getInstance() {
        return instance;
    }


    private ShadowDataSourceService() {

        init();
    }

    /**
     * 初始化datasource
     */
    private void init() {
        String value = "";
        try {
            value = OptimusConfig.getValue(STRESS_DATASOURCE_MAPPING);

            log.info("$ {stress.datasource.onlineToshdaow.mapping} value from apollo. value:{}", value);

            /*value = "[{\n" +
                    "            \"onlinedataSource\":{\"url\":\"xxx\",\"username\":\"name\",\"password\":\"pass\"},\n" +
                    "            \"shadowdataSource\":{\"url\":\"xxx\",\"username\":\"name\",\"password\":\"pass\"}\n" +
                    "        },{\n" +
                    "            \"onlinedataSource\":{\"url\":\"xxx2\",\"username\":\"name\",\"password\":\"pass\"},\n" +
                    "            \"shadowdataSource\":{\"url\":\"xxx2\",\"username\":\"name\",\"password\":\"pass\"}\n" +
                    "        }]";*/

           /* [{
                "onlinedataSource":{"url":"jdbc:mysql://mysql1.dev.scsite.net:3306/fin_repayment?allowMultiQueries=true","username":"root","password":"dpjA8Z6XPXbvos"},
                "shadowdataSource":{"url":"jdbc:mysql://mysql1.dev.scsite.net:3306/fin_repayment_test?allowMultiQueries=true","username":"root","password":"dpjA8Z6XPXbvos"}
            }]*/
            JSONArray jsonArray = null;
            try {
                jsonArray = JSON.parseArray(value);
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("$ {stress.datasource.onlineToshdaow.mapping} value:[%s] is not right.", value));
            }

            if (jsonArray == null || jsonArray.size() == 0) {
                throw new IllegalArgumentException(String.format("$ {stress.datasource.onlineToshdaow.mapping} value:[%s] is not right or not configure.", value));
            }
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                DataSourceConfig dataSourceConfigOnline = this.getDataSourceConfig(jsonObject, ONLINEDATA_SOURCE);

                DataSourceConfig dataSourceConfigShadow = this.getDataSourceConfig(jsonObject, SHADOWDATA_SOURCE);

                if (dataSourceConfigOnline != null && dataSourceConfigShadow != null) {

                    dataSourceConfigToShadowConfig.put(dataSourceConfigOnline, dataSourceConfigShadow);

                    urlDataSourceConfigToShadowConfig.put(dataSourceConfigOnline.url, dataSourceConfigShadow);
                }
            }
        } catch (Exception e) {
            log.error("fail to get shadow datasource. value:{}", value, e);
            throw new IllegalArgumentException(String.format("$ {stress.datasource.onlineToshdaow.mapping} value:[%s] is not right.", value));
        }
    }

    /**
     * 获取数据源config
     *
     * @param jsonObject
     * @param dataSourceName
     * @return
     */
    public DataSourceConfig getDataSourceConfig(JSONObject jsonObject, String dataSourceName) {

        JSONObject onlinedataSourceJsonObject = (JSONObject) jsonObject.get(dataSourceName);

        DataSourceConfig dataSourceConfig = null;
        if (onlinedataSourceJsonObject != null) {
            dataSourceConfig = new DataSourceConfig(
                    getTrimedUrl(onlinedataSourceJsonObject.getString(URL)),
                    onlinedataSourceJsonObject.getString(USERNAME),
                    onlinedataSourceJsonObject.getString(PASSWORD));

        }
        return dataSourceConfig;
    }

    /**
     * @param url jdbc:mysql://mysql1.dev.scsite.net:3306/fin_repayment?allowMultiQueries=true
     * @return
     */
    public String getTrimedUrl(String url) {

        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("url can not be empty." + url);
        }
        try {
            //如果jdbc:mysql://mysql1.dev.scsite.net:3306/fin_repayment 这种格式，直接返回trim后结果
            if (!url.contains("?")) {
                return url.trim();
            }
            return url.substring(url.indexOf(DATASOURCE_URL_PREFIX), url.lastIndexOf("?"));
        } catch (Exception e) {
            throw new IllegalArgumentException("url format is not right,value:" + url);
        }

    }

    public DataSourceConfig getShadowDataSourceByUrl(String url) {

        return urlDataSourceConfigToShadowConfig.get(this.getTrimedUrl(url));
    }

    /**
     *
     */
    public static class DataSourceConfig implements Serializable {

        private String username;

        private String password;

        //jdbc:mysql://mysql1.dev.scsite.net:3306/test
        private String url;

        Matcher matcher = null;

        public DataSourceConfig(String url, String username, String password) {
            this.username = username;
            this.password = password;
            this.url = url;
            matcher = pattern.matcher(url);
            if (!matcher.matches()) {
                throw new RuntimeException("db url format is wrong,url:" + url);
            }
        }


        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getUrl() {
            return url;
        }

        /**
         * jdbc:mysql://mysql1.dev.scsite.net:3306/test
         *
         * @return
         */
        public String getHost() {
            return matcher.group(1);
        }

        /**
         * jdbc:mysql://mysql1.dev.scsite.net:3306/test
         *
         * @return
         */
        public int getPort() {

            try {
                return Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) {
                log.error("fail to parseInt port ,the url is wrong,url:{}", url, e);
            }
            return 0;
        }

        /**
         * jdbc:mysql://mysql1.dev.scsite.net:3306/test
         *
         * @return
         */
        public String getDatabase() {

            return matcher.group(3);

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataSourceConfig that = (DataSourceConfig) o;
            return Objects.equals(username, that.username) &&
                    Objects.equals(password, that.password) &&
                    Objects.equals(url, that.url);
        }

        @Override
        public int hashCode() {

            return Objects.hash(username, password, url);
        }
    }


    public Map<DataSourceConfig, DataSourceConfig> getDataSourceConfigToShadowConfig() {
        return dataSourceConfigToShadowConfig;
    }


}
