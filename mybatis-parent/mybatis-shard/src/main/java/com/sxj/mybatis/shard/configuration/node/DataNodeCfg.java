package com.sxj.mybatis.shard.configuration.node;

public class DataNodeCfg
{
    
    private String writeNodes;
    
    private String readNodes;
    
    private String tables;
    
    private String writeTables;
    
    private String readTables;
    
    @Override
    public String toString()
    {
        return "DataNode [writeNodes=" + writeNodes + ", readNodes="
                + readNodes + "]";
    }
    
    public String getWriteNodes()
    {
        return writeNodes;
    }
    
    public void setWriteNodes(String writeNodes)
    {
        this.writeNodes = writeNodes;
    }
    
    public String getReadNodes()
    {
        return readNodes;
    }
    
    public void setReadNodes(String readNodes)
    {
        this.readNodes = readNodes;
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
    
    public String getReadTables()
    {
        return readTables;
    }
    
    public void setReadTables(String readTables)
    {
        this.readTables = readTables;
    }
    
}
