/*
 * @(#)MyBatisTest.java 2013年12月23日 下午23:33:33
 *
 * Copyright (c) 2011-2013 Makersoft.org all rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 */
package com.codefarm.mybatis.orm.unit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.codefarm.mybatis.orm.mapper.UserMapper;
import com.codefarm.mybatis.orm.model.TestUser;
import com.codefarm.mybatis.orm.po.TestUserCriterias;

/**
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@ActiveProfiles("test")
public class UserMapperTest
{
    
    @Autowired(required = true)
    private UserMapper userMapper;
    
    private static TestUser buildUser()
    {
        TestUser user = new TestUser();
        user.setUserName("makersoft");
        return user;
    }
    
    @Transactional
    public void testInsert()
    {
        //        TestUser user = buildUser();
        //        user.setId(13333333l);
        //        int rows = userMapper.insertUser(user);
        //        user.setUserName("2");
        //        userMapper.insert(user);
        //        assertTrue("Insert entity not success!", rows > 0);
        //        assertNotNull("Id can not be null!", user.getId());
        
    }
    
    @Transactional
    public void testUpdate()
    {
        //        TestUser entity = userMapper.getById(1L);
        //        assertNotNull("selected entity can not be null!", entity);
        //        
        //        TestUser newUser = new TestUser(entity.getId());
        //        newUser.setUserName("my_name");
        //        int rows = userMapper.update(newUser);
        //        
        //        assertTrue("Update entity not success!", rows == 1);
    }
    
    @Test
    @Transactional
    public void testGet()
    {
        TestUser entity = userMapper.getById(17L);
        assertNotNull(entity);
        assertTrue(entity.getId() == 17l);
        
        List<TestUser> select = userMapper.select(18l, "2");
        assertNotNull(select);
        assertTrue(select.size() == 1);
        TestUserCriterias criterias = new TestUserCriterias();
        criterias.setUserid(15l);
        List<TestUser> select2 = userMapper.selectByCriterias(criterias);
        assertNotNull(select2);
        List<TestUser> select4 = userMapper
                .selectByIds(new Long[] { 15l, 16l });
        assertTrue(select4.size() == 2);
        //        TestUserCriterias criterias2 = new TestUserCriterias();
        //        criterias2.setUserids(new Long[] { 15l, 16l });
        //        List<TestUser> select5 = userMapper.selectByCriterias(criterias2);
        //        assertTrue(select5.size() == 2);
    }
    
    @Transactional
    public void testDelete()
    {
        //        int rows = userMapper.delete(17l);
        //        assertTrue(rows == 1);
        //        rows = userMapper.deleteByIdAndName(15l, "2");
        //        assertTrue(rows == 0);
        //        rows = userMapper.deleteByIdAndName(15l, "makersoft");
        //        assertTrue(rows == 1);
        //        TestUserCriterias criterias = new TestUserCriterias();
        //        criterias.setUserid(15l);
        //        rows = userMapper.deleteByCriteriasAndUserName(criterias, "2");
        //        assertTrue("Delete operation could not success!", rows > 1);
        //        rows = userMapper.deleteByCriterias(criterias);
        //        assertTrue(rows > 1);
        
    }
    
}
