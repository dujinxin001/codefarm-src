package com.sxj.mybatis.dialect;

public class MySql5PageHepler
{
    /**
     * 得到查询总数的sql
     */
    public static String getCountString(String querySelect)
    {
        
        return CountHelper.getCountString(querySelect);
    }
    
    /**
     * 得到分页的SQL
     * 
     * @param offset
     *            偏移量
     * @param limit
     *            位置
     * @return 分页SQL
     */
    public static String getLimitString(String querySelect, int offset,
            int limit)
    {
        
        querySelect = CountHelper.getLineSql(querySelect);
        
        String sql = querySelect + " limit "
                + offset + " ," + limit;
        
        return sql;
        
    }
    
}
