package com.sxj.mybatis.shard.entity;

import com.sxj.mybatis.orm.annotations.Column;
import com.sxj.mybatis.orm.annotations.Entity;
import com.sxj.mybatis.orm.annotations.GeneratedValue;
import com.sxj.mybatis.orm.annotations.GenerationType;
import com.sxj.mybatis.orm.annotations.Id;
import com.sxj.mybatis.orm.annotations.Table;
import com.sxj.mybatis.shard.dao.Shard2Mapper;

@Entity(mapper = Shard2Mapper.class)
@Table(name = "SHARD2")
public class Shard2
{
    @Id(column = "SHARD2_ID")
    @GeneratedValue(strategy = GenerationType.UUID, length = 4)
    private String shard2Id;
    
    @Column(name = "SHARD2_NAME")
    private String shard2Name;
    
    public String getShard2Id()
    {
        return shard2Id;
    }
    
    public void setShard2Id(String shard2Id)
    {
        this.shard2Id = shard2Id;
    }
    
    public String getShard2Name()
    {
        return shard2Name;
    }
    
    public void setShard2Name(String shard2Name)
    {
        this.shard2Name = shard2Name;
    }
}
