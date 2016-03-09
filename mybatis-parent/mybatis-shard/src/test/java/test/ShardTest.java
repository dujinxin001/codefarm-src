package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.codefarm.mybatis.shard.dao.ShardMapper;
import com.codefarm.mybatis.shard.entity.Shard;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-shard.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
public class ShardTest
{
    
    // private BeanFactory factory;
    
    //    @Before
    //    public void before() throws Exception
    //    {
    //        factory = new ClassPathXmlApplicationContext("/spring-shard.xml");
    //    }
    @Autowired
    ShardMapper mapper;
    
    @Test
    @Transactional
    public void testInsert()
    {
        Shard shard = new Shard();
        //        shard.setShardId(4);
        shard.setShardName("test测试SHARD");
        mapper.insert(shard);
        mapper.insert(shard);
        mapper.insert(shard);
        mapper.insert(shard);
        mapper.insert(shard);
        mapper.insert(shard);
        mapper.insert(shard);
        mapper.insert(shard);
        mapper.insert(shard);
        mapper.insert(shard);
        mapper.insert(shard);
        mapper.insert(shard);
        //		BlogMapper mapper = factory.getBean(BlogMapper.class);
        //		BlogExample ex = new BlogExample();
        //		ex.createCriteria().andTitleLike("%").andIdIsNotNull().andUserIdEqualTo(32);
        //
        //		Blog record = new Blog();
        //		record.setContext("aaaaaa");
        //		record.setCreateTime(new Date());
        //		record.setIsUse(true);
        //		mapper.updateByExampleSelective(record, ex);
    }
    
    public void testGet()
    {
        Shard shard = mapper.get(2);
        System.out.println(shard.getShardName());
    }
    
}
