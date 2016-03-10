package com.codefarm.mybatis.shard.dao;

import com.codefarm.mybatis.orm.annotations.Select;
import com.codefarm.mybatis.orm.annotations.Insert;
import com.codefarm.mybatis.shard.entity.Shard;

public interface ShardMapper
{
    @Insert
    public void insert(Shard shard);
    
    @Select
    public Shard get(int shardId);
    
    public void insert2(Shard shard);
    
    public Shard get2(int shardId);
}
