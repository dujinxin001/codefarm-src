package test;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.codefarm.mybatis.shard.dao.Shard2Mapper;
import com.codefarm.mybatis.shard.entity.Shard2;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-shard.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class Shard2Test
{
    @Autowired
    Shard2Mapper mapper;
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }
    
    public void testInsert()
    {
        Shard2 shard = new Shard2();
        //        shard.setShard2Id("e");
        shard.setShard2Name("c测试");
        mapper.insert(shard);
    }
    
    @Test
    public void testGet()
    {
        Shard2 shard2 = mapper.get("6u8h");
        System.out.println(shard2.getShard2Name());
    }
    
}
