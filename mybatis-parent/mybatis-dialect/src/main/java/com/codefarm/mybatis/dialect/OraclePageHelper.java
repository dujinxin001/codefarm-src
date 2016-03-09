package com.codefarm.mybatis.dialect;

public class OraclePageHelper
{
    
    public static String getLimitString(String querySelect, int offset,
            int limit)
    {
        querySelect = querySelect.trim();
        boolean isForUpdate = false;
        if (querySelect.toLowerCase().endsWith(" for update"))
        {
            querySelect = querySelect.substring(0, querySelect.length() - 11);
            isForUpdate = true;
        }
        
        StringBuffer pagingSelect = new StringBuffer(querySelect.length() + 100);
        
        pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
        
        pagingSelect.append(querySelect);
        
        pagingSelect.append(" ) row_ ) where rownum_ > " + offset
                + " and rownum_ <= " + (offset + limit));
        
        if (isForUpdate)
        {
            pagingSelect.append(" for update");
        }
        
        return pagingSelect.toString();
    }
    
    public static String getCountString(String selectSql)
    {
        return CountHelper.getCountString(selectSql);
    }
}
