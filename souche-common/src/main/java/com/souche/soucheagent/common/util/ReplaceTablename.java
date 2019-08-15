package com.souche.soucheagent.common.util;


import org.apache.commons.lang3.StringUtils;

/**
 * This demo shows how to replace a specified table name with new one.
 * <p>Steps to modify a table name:
 * <p>1. find the table name that need to be replaced.
 * <p>2. use setString method to set a new string representation of that table.
 * <p>
 * <p>In this demo, input sql is:
 * <p>
 * <p>select table1.col1, table2.col2
 * <p>from table1, table2
 * <p>where table1.foo > table2.foo
 * <p>
 * <p>we want to replace table2 with "(tableX join tableY using (id)) as table3"
 * <p>and change table2.col2 to table3.col2, table2.foo to table3.foo accordingly.
 */
/**
 * @Author: jiabangkang
 * @Date: 2019/6/11 上午9:10
 * @Version: 1.0
 * @Description: 
 */
public class ReplaceTablename {


    //反单引号
    public static final String BACKQUOTE = "`";

    /**
     * 表添加后缀名
     *
     * @param sql
     * @param suffix
     */
    public static String renameTable(String sql, String suffix) {

        try {
            return  TableRenameUtil.modifyTableNames(sql, new TableRenameUtil.TableRenamer() {
                @Override
                public String rename(String oldTableName) {

                    if (StringUtils.isNotEmpty(oldTableName) && oldTableName.endsWith(BACKQUOTE)) {

                        return oldTableName.substring(0,oldTableName.length()-1) + Constants.SHADOW_SUFFIX + BACKQUOTE;
                    }
                     return oldTableName + Constants.SHADOW_SUFFIX;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            return sql;
        }

    }


    public static void main(String[] args) {
        String sql = "insert into `tb_user` values(\'3\',\'kang\',\'1211\',\'12446\',now())";
        String sql2 = "insert into tb_user values(\'3\',\'kang\',\'1211\',\'12446\',now())";
        System.out.println(renameTable(sql,Constants.SHADOW_SUFFIX));
        System.out.println(renameTable(sql2,Constants.SHADOW_SUFFIX));
    }


}
