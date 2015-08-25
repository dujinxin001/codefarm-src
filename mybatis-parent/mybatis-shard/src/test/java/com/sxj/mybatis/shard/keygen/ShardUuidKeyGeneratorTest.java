package com.sxj.mybatis.shard.keygen;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;

import com.sxj.mybatis.dialect.MySql5Dialect;
import com.sxj.mybatis.orm.keygen.ShardUuidKeyGenerator;
import com.sxj.mybatis.shard.entity.Shard2;

public class ShardUuidKeyGeneratorTest
{
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }
    
    @Test
    public void test() throws SQLException
    {
        ShardUuidKeyGenerator keyGenerator = new ShardUuidKeyGenerator(62);
        List<Shard2> shards = new ArrayList<Shard2>();
        Shard2 shard1 = new Shard2();
        shard1.setShard2Name("1");
        Shard2 shard2 = new Shard2();
        shard2.setShard2Name("2");
        shards.add(shard1);
        shards.add(shard2);
        keyGenerator.process(null, shards, new MySql5Dialect());
    }
    
}
