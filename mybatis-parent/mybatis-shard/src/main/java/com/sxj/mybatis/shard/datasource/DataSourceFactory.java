package com.sxj.mybatis.shard.datasource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.sxj.mybatis.shard.configuration.XmlReader;
import com.sxj.mybatis.shard.configuration.node.DataNodeCfg;
import com.sxj.mybatis.shard.configuration.node.KeyNodeCfg;
import com.sxj.mybatis.shard.configuration.node.KeyNodeType;
import com.sxj.mybatis.shard.configuration.node.ShardRuleCfg;

public class DataSourceFactory
{
    
    private static List<DataSourceNode> dataNodes;
    
    private static List<DataSource> keyGeneratorDs;
    
    private static List<DataSource> snGeneratorDs;
    
    private static Map<String, String> shardTables = new HashMap<String, String>();
    
    private static ApplicationContext context;
    
    private static boolean initialized = false;
    
    public static void main(String[] args)
    {
        initDataSources();
    }
    
    public static List<DataSourceNode> getDataNodes()
    {
        if (dataNodes == null)
        {
            initDataSources();
        }
        return dataNodes;
    }
    
    public static List<DataSourceNode> getDataNodes(String tableName,
            String command)
    {
        if (dataNodes == null)
            initDataSources();
        return filter(tableName, command);
    }
    
    public static List<DataSourceNode> getDataNodes(String tableName)
    {
        if (dataNodes == null)
            initDataSources();
        return filter(tableName, null);
    }
    
    private static List<DataSourceNode> filter(String tableName, String command)
    {
        if (StringUtils.isEmpty(tableName))
            return dataNodes;
        List<DataSourceNode> result = new ArrayList<DataSourceNode>();
        for (DataSourceNode node : dataNodes)
        {
            String tables = node.getTables();
            if (StringUtils.isEmpty(tables))
                result.add(node);
            else
            {
                String[] split = tables.split(",");
                if (Arrays.asList(split).contains(tableName))
                    result.add(node);
            }
        }
        return result;
    }
    
    public static Map<String, String> getShardTables()
    {
        if (shardTables.size() == 0)
        {
            initDataSources();
        }
        return shardTables;
    }
    
    public static void initDataSources()
    {
        if (initialized)
            return;
        
        XmlReader.loadShardConfigs();
        //        Map<String, DataSourceCfg> dataSourceCfgs = XmlReader.getDataSources();
        List<DataNodeCfg> dataNodeCfgs = XmlReader.getDataNodes();
        Map<String, ShardRuleCfg> ruleCfgs = XmlReader.getRules();
        List<KeyNodeCfg> keyNodeCfgs = XmlReader.getKeyNodeCfgs(KeyNodeType.ID);
        List<KeyNodeCfg> snNodeCfgs = XmlReader.getKeyNodeCfgs(KeyNodeType.SN);
        try
        {
            //            Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();
            //            for (Map.Entry<String, DataSourceCfg> entry : dataSourceCfgs.entrySet())
            //            {
            //                Properties p = new Properties();
            //                p.setProperty("driverClassName", "com.mysql.jdbc.Driver");
            //                p.setProperty("url", entry.getValue().getUrl());
            //                p.setProperty("username", entry.getValue().getUserName());
            //                p.setProperty("password", entry.getValue().getPassword());
            //                
            //                p.setProperty("maxActive", "1");
            //                p.setProperty("maxIdle", "1");
            //                p.setProperty("minIdle", "1");
            //                
            //                DataSource ds = BasicDataSourceFactory.createDataSource(p);
            //                
            //                dataSourceMap.put(entry.getValue().getName(), ds);
            //            }
            
            dataNodes = new ArrayList<DataSourceNode>();
            keyGeneratorDs = new ArrayList<DataSource>();
            for (KeyNodeCfg keyNodeCfg : keyNodeCfgs)
            {
                List<String> kStr = split(keyNodeCfg.getKeyNodes(), ",");
                for (String str : kStr)
                {
                    keyGeneratorDs.add(context.getBean(str, DataSource.class));
                }
            }
            
            snGeneratorDs = new ArrayList<DataSource>();
            for (KeyNodeCfg snNodeCfg : snNodeCfgs)
            {
                List<String> kStr = split(snNodeCfg.getKeyNodes(), ",");
                for (String str : kStr)
                {
                    snGeneratorDs.add(context.getBean(str, DataSource.class));
                }
            }
            
            for (DataNodeCfg cfg : dataNodeCfgs)
            {
                List<DataSource> w = new ArrayList<DataSource>();
                List<DataSource> r = new ArrayList<DataSource>();
                
                List<String> wStr = split(cfg.getWriteNodes(), ",");
                for (String str : wStr)
                {
                    w.add(context.getBean(str, DataSource.class));
                }
                List<String> rStr = split(cfg.getReadNodes(), ",");
                for (String str : rStr)
                {
                    r.add(context.getBean(str, DataSource.class));
                }
                
                DataSourceNode dsNode = new DataSourceNode(w, r);
                dsNode.setTables(cfg.getTables());
                dsNode.setWriteTables(cfg.getWriteTables());
                dsNode.setReadTables(cfg.getReadTables());
                dataNodes.add(dsNode);
            }
            
            for (Map.Entry<String, ShardRuleCfg> entry : ruleCfgs.entrySet())
            {
                shardTables.put(entry.getKey(), entry.getValue().getColumn());
            }
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        initialized = true;
    }
    
    private static List<String> split(String input, String s)
    {
        if (input == null)
        {
            return null;
        }
        List<String> list = new ArrayList<String>();
        String[] tmp = input.split(s);
        for (String str : tmp)
        {
            if (str == null || str.matches("\\s*"))
            {
                continue;
            }
            list.add(str.trim());
            
        }
        return list;
    }
    
    public DataSource getDefault()
    {
        return null;
    }
    
    public DataSource getByHash()
    {
        return null;
    }
    
    public static class DataSourceNode
    {
        
        private List<DataSource> writeNodes;
        
        private List<DataSource> readNodes;
        
        private String tables;
        
        private String writeTables;
        
        private String readTables;
        
        public DataSourceNode(List<DataSource> writeNodes,
                List<DataSource> readNodes)
        {
            this.writeNodes = writeNodes;
            this.readNodes = readNodes;
        }
        
        public List<DataSource> getWriteNodes()
        {
            return writeNodes;
        }
        
        public List<DataSource> getReadNodes()
        {
            return readNodes;
        }
        
        public String getTables()
        {
            return tables;
        }
        
        public void setTables(String tables)
        {
            this.tables = tables;
        }
        
        public String getWriteTables()
        {
            return writeTables;
        }
        
        public void setWriteTables(String writeTables)
        {
            this.writeTables = writeTables;
        }
        
        public void setWriteNodes(List<DataSource> writeNodes)
        {
            this.writeNodes = writeNodes;
        }
        
        public String getReadTables()
        {
            return readTables;
        }
        
        public void setReadTables(String readTables)
        {
            this.readTables = readTables;
        }
        
    }
    
    public static void setContext(ApplicationContext context)
    {
        DataSourceFactory.context = context;
    }
    
    public static List<DataSource> getKeyGeneratorDs()
    {
        if (dataNodes == null)
        {
            initDataSources();
        }
        return keyGeneratorDs;
    }
    
    public static List<DataSource> getSnGeneratorDs()
    {
        if (dataNodes == null)
        {
            initDataSources();
        }
        return snGeneratorDs;
    }
    
}
