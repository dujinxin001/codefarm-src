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

/**
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
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
        TestUser user = buildUser();
        user.setId(13333333l);
        int rows = userMapper.insertUser(user);
        user.setUserName("2");
        userMapper.insert(user);
        assertTrue("Insert entity not success!", rows > 0);
        assertNotNull("Id can not be null!", user.getId());
        
    }
    
    @Transactional
    public void testUpdate()
    {
        TestUser entity = userMapper.get(1L);
        assertNotNull("selected entity can not be null!", entity);
        
        TestUser newUser = new TestUser(entity.getId());
        newUser.setUserName("my_name");
        int rows = userMapper.update(newUser);
        
        assertTrue("Update entity not success!", rows == 1);
    }
    
    @Test
    @Transactional
    public void testGet()
    {
        TestUser entity = userMapper.get(17L);
        System.out.println(entity.getUserName());
        entity = userMapper.getById(18L);
        System.out.println(entity.getUserName());
        assertNotNull("selected entity can not be null!", entity);
    }
    
    @Transactional
    public void testDelete()
    {
        TestUser user = userMapper.get(1L);
        user = userMapper.get(user.getId());
        
        int rows = userMapper.delete(user);
        
        assertTrue("Delete operation could not success!", rows == 1);
    }
    
}