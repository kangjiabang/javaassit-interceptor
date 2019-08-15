package com.souche.soucheagent.common.util;


import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description: 基于JsqlParser的sql解析替换工具类
 * @author zjc
 * @since 1.0.0
 */
public class TableRenameUtil
{
   // private static  ConcurrentMap<String,String> dealedSqlMap = new ConcurrentHashMap<String,String>();


    final static int cacheSize = 1000;
    //LRU cache
    private static Map<String, String> dealedSqlMap = new LinkedHashMap<String, String>((int) Math.ceil(cacheSize / 0.75f) + 1, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > cacheSize;
        }
    };

    public interface TableRenamer
    {
        public String rename(String oldTableName);
    }



    public static String modifyTableNames(String sql,TableRenamer tableRenamer)
    {

        if(sql == null)
        {
            throw new IllegalArgumentException("sql is null");
        }

        //如果不为空，返回缓存的sql数据
        if (dealedSqlMap.get(sql) != null) {
            return dealedSqlMap.get(sql);
        }
        Statement statement = null;
        try
        {
            statement = CCJSqlParserUtil.parse(sql);
        }
        catch (JSQLParserException e)
        {
            //throw new IllegalArgumentException("Error when parsing sql:[" + sql+"]",e);
            return sql;
        }

        TableRenameVisitor tableRenameVisitor=new TableRenameVisitor(tableRenamer);
        statement.accept(tableRenameVisitor);
        String dealedSql = statement.toString();
        dealedSqlMap.put(sql,dealedSql);

        return dealedSql;
    }
}
