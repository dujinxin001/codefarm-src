package com.codefarm.mybatis.shard.entity;

import com.codefarm.mybatis.orm.annotations.Column;
import com.codefarm.mybatis.orm.annotations.Entity;
import com.codefarm.mybatis.orm.annotations.GeneratedValue;
import com.codefarm.mybatis.orm.annotations.Id;
import com.codefarm.mybatis.orm.annotations.Sn;
import com.codefarm.mybatis.orm.annotations.Table;
import com.codefarm.mybatis.shard.dao.ShardMapper;

@Entity(mapper = ShardMapper.class)
@Table(name = "SHARD")
public class Shard
{
    @Id(column = "SHARD_ID")
    @GeneratedValue
    private long shardId;
    
    @Column(name = "SHARD_NAME")
    private String shardName;
    
    @Column(name = "SERIAL_NUMBER")
    @Sn(stubValue = "SHARD")
    private String serialNumber;
    
    public String getShardName()
    {
        return shardName;
    }
    
    public void setShardName(String shardName)
    {
        this.shardName = shardName;
    }
    
    public long getShardId()
    {
        return shardId;
    }
    
    public void setShardId(long shardId)
    {
        this.shardId = shardId;
    }
    
    public String getSerialNumber()
    {
        return serialNumber;
    }
    
    public void setSerialNumber(String serialNumber)
    {
        this.serialNumber = serialNumber;
    }
}
