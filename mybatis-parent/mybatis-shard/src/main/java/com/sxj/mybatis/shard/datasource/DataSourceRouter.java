package com.sxj.mybatis.shard.datasource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sxj.mybatis.shard.MybatisConfiguration;
import com.sxj.mybatis.shard.datasource.DataSourceFactory.DataSourceNode;
import com.sxj.mybatis.shard.transaction.ShardManagedTransactionManager;
import com.sxj.spring.modules.util.RegexUtil;

public class DataSourceRouter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceRouter.class);
    
    // 数据节点数量
    // private static int nodeNum = DataSourceFactory.getNodes().size();
    
    private static final ThreadLocal<Set<String>> writeTableQueue = new ThreadLocal<Set<String>>()
    {
        
        @Override
        protected Set<String> initialValue()
        {
            return new HashSet<String>();
        }
        
    };
    
    public final static String Command_W = "w";
    
    public final static String Command_R = "r";
    
    public static void main(String[] args)
    {
        String s = "";
        s = " update   able set aa = 1;";
        s = "	insert 	into talbe_ads values()";
        s = "select    * from aaa where aadd=adea ";
        s = "delete from adeaadd ";
        
        s = "update sample_blog set aad=12,wee=2 where  user_id  = ? and abc = ? and a=? and def =?";
        System.out.println(HashCodeBuilder.reflectionHashCode(s + "2", true));
        // s =
        // "insert into sample_blog (context,  user_id, title) values ('\'asdf\'asdf\'asdf,,,,()', 122,'12341');";
        // s = "delete from sample_blog where aaa like ? and user_id = ?";
        // s =
        // "select * from sample_blog where asd like ? and ssea <> ? and asdf != ? and user_id = ?";
        // getDsIndex(s);
        // s =
        // "update sample_blog set context = ? where ( title like ? and id is not null and user_id = ? and ( aa like ? or ads like ?) )";
        // BoundSql bs = new BoundSql(configuration, s, null, null);
        // getDataSource(null, bs, null);
    }
    
    public static DataSource getKeyGeneratorDataSource()
    {
        List<DataSource> keyGeneratorDs = DataSourceFactory.getKeyGeneratorDs();
        return keyGeneratorDs.get((int) (keyGeneratorDs.size() * Math.random()));
    }
    
    public static List<DataSource> getKeyGeneratorDataSources()
    {
        return DataSourceFactory.getKeyGeneratorDs();
    }
    
    public static List<DataSource> getSnGeneratorDataSources()
    {
        return DataSourceFactory.getSnGeneratorDs();
    }
    
    public static DataSource getDataSource(MappedStatement ms,
            BoundSql boundSql, Object param)
    {
        String sql = boundSql.getSql();
        sql = sql.replaceAll("\\s+", " ");
        String lowerSql = sql.trim().toLowerCase();
        String commandName = getCommand(lowerSql);
        String commandType = null;
        String tblName = null;
        
        if (commandName.equals("select"))
        {
            commandType = Command_R;
        }
        else
        {
            commandType = Command_W;
            refreshTableQueue(tblName);
        }
        
        // for update sql : update t set xxx where aa = 22
        if (commandName.equals("update"))
        {
            tblName = sql.substring(7, sql.indexOf(" ", 7));
        }
        // for insert sql
        else if (commandName.equals("insert"))
        {
            tblName = sql.substring(12, sql.indexOf(" ", 12));
        }
        // for select or delete
        else
        {
            int index = lowerSql.indexOf("from");
            int sIndex = index + 5;
            int eIndex = sql.indexOf(" ", sIndex);
            if (eIndex == -1)
            {
                tblName = sql.substring(sIndex);
            }
            else
            {
                tblName = sql.substring(sIndex, eIndex);
            }
        }
        
        if (tblName == null)
        {
            throw new IllegalArgumentException("wrong sql");
        }
        
        DataSourceNode targetNode = null;
        
        String columnRule = DataSourceFactory.getShardTables().get(tblName);
        // don't need to shard, return the first dataSourceNode
        if (columnRule == null)
        {
            targetNode = DataSourceFactory.getDataNodes().get(0);
        }
        else
        {
            // shard column value
            int shardValueIndex = -1;
            
            // for sql contains where
            if (commandName.equals("update") || commandName.equals("select")
                    || commandName.equals("delete"))
            {
                int index = 0;
                if (commandName.equals("update"))
                {
                    String setStr = RegexUtil.substr(lowerSql, "set", "where");
                    index = setStr.split(",").length;
                }
                String whereStr = lowerSql.substring(lowerSql.indexOf("where") + 5);
                whereStr = whereStr.replaceAll("\\(", "");
                whereStr = whereStr.replaceAll("\\)", "");
                String[] wheres = whereStr.split(" and ");
                for (String str : wheres)
                {
                    for (String t : str.split(" or "))
                    {
                        if (t.contains("?"))
                        {
                            if (t.trim().startsWith(columnRule.toLowerCase()))
                            {
                                shardValueIndex = index;
                                break;
                            }
                            index++;
                        }
                    }
                }
                
            }
            // for insert sql
            else if (commandName.equals("insert"))
            {
                String insertColumnStrs = RegexUtil.substr(sql, "(", ")");
                
                String[] columns = insertColumnStrs.split(",");
                int i = 0;
                for (String col : columns)
                {
                    col = col.trim();
                    if (col.equals(columnRule))
                    {
                        shardValueIndex = i;
                        break;
                    }
                    i++;
                }
                
                String insertValues = sql.substring(lowerSql.indexOf("values") + 6)
                        .trim();
                insertValues = insertValues.substring(1,
                        insertValues.length() - 1);
                insertValues = insertValues.replaceAll("\\(.*?\\)", "");
                String[] insertVs = insertValues.split(",");
                for (int j = 0; j < insertVs.length; j++)
                {
                    if (j >= i)
                    {
                        break;
                    }
                    
                    if (!(insertVs[j].trim()).equals("?"))
                    {
                        shardValueIndex--;
                    }
                }
            }
            
            if (shardValueIndex == -1)
            {
                targetNode = DataSourceFactory.getDataNodes(tblName).get(0);
                // throw new UnsupportedOperationException(
                // "need shard param in where case for column:"
                // + columnRule);
            }
            else
            {
                
                Object shardValue = getParamValue(ms,
                        boundSql,
                        param,
                        shardValueIndex);
                int value = -1;
                if (shardValue instanceof Integer)
                {
                    value = (Integer) shardValue;
                }
                else if (shardValue instanceof Long)
                {
                    value = ((Long) shardValue).intValue();
                }
                else if (shardValue instanceof String)
                {
                    value = HashCodeBuilder.reflectionHashCode(shardValue, true);
                }
                else if (shardValue == null)
                {
                    value = 0;
                }
                else
                {
                    
                    throw new UnsupportedOperationException(
                            "shard value must be int or long");
                }
                List<DataSourceNode> availableNodes = DataSourceFactory.getDataNodes(tblName);
                int nodeIndex = (int) (value % availableNodes.size());
                targetNode = availableNodes.get(nodeIndex);
            }
        }
        
        DataSource ds = null;
        if (commandType.equals(Command_R))
        {
            int index = (int) (targetNode.getReadNodes().size() * Math.random());
            if (!isWriteTable(tblName)
                    && ShardManagedTransactionManager.isCurrentTransactionReadOnly())
            {
                ds = targetNode.getReadNodes().get(index);
                LOGGER.debug("Will Execute in READ Mode:|=======" + lowerSql);
            }
            else
            {
                ds = targetNode.getWriteNodes().get(index);
                LOGGER.debug("Will Execute in WRITE Mode:|=======" + lowerSql);
            }
        }
        else
        {
            int index = (int) (targetNode.getWriteNodes().size() * Math.random());
            ds = targetNode.getWriteNodes().get(index);
            LOGGER.debug("Will Execute in WRITE Mode:|=======" + lowerSql);
        }
        
        return ds;
    }
    
    private static boolean refreshTableQueue(String tblName)
    {
        Set<String> set = writeTableQueue.get();
        if (set.contains(tblName))
            return false;
        set.add(tblName);
        writeTableQueue.set(set);
        return true;
    }
    
    private static boolean isWriteTable(String tblName)
    {
        return writeTableQueue.get().contains(tblName);
    }
    
    private static Object getParamValue(MappedStatement ms, BoundSql boundSql,
            Object param, int index)
    {
        TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration()
                .getTypeHandlerRegistry();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        MetaObject metaObject = param == null ? null
                : MybatisConfiguration.getConfiguration().newMetaObject(param);
        
        ParameterMapping parameterMapping = parameterMappings.get(index);
        
        Object value = null;
        if (parameterMapping.getMode() != ParameterMode.OUT)
        {
            
            String propertyName = parameterMapping.getProperty();
            PropertyTokenizer prop = new PropertyTokenizer(propertyName);
            if (param == null)
            {
                value = null;
            }
            else if (typeHandlerRegistry.hasTypeHandler(param.getClass()))
            {
                value = param;
            }
            else if (boundSql.hasAdditionalParameter(propertyName))
            {
                value = boundSql.getAdditionalParameter(propertyName);
            }
            else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX)
                    && boundSql.hasAdditionalParameter(prop.getName()))
            {
                value = boundSql.getAdditionalParameter(prop.getName());
                if (value != null)
                {
                    value = MybatisConfiguration.getConfiguration()
                            .newMetaObject(value)
                            .getValue(propertyName.substring(prop.getName()
                                    .length()));
                }
            }
            else
            {
                value = metaObject == null ? null
                        : metaObject.getValue(propertyName);
            }
        }
        
        return value;
    }
    
    private static String getCommand(String sql)
    {
        int index = sql.indexOf(" ");
        if (index == -1)
        {
            throw new IllegalArgumentException();
        }
        String tmp = sql.substring(0, index);
        return tmp;
    }
    
}
