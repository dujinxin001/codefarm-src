package com.codefarm.mybatis.shard.dao;

import com.codefarm.mybatis.orm.annotations.Get;
import com.codefarm.mybatis.orm.annotations.Insert;
import com.codefarm.mybatis.shard.entity.Shard2;

public interface Shard2Mapper
{
    @Get
    public Shard2 get(String shard2Id);
    
    @Insert
    public void insert(Shard2 shard2);
    
    public Shard2 get2(String shard2Id);
    
    public void insert2(Shard2 shard2);
}
