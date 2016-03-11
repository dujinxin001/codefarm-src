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

import java.util.ArrayList;
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
        return user;
    }
    
    @Transactional
    public void testInsert()
    {
        //---------insert single object-----------//
        TestUser user = buildUser();
        user.setId(13333333l);
        int rows = userMapper.insertUser(user);
        assertTrue(rows == 1);
        
        user.setUserName("2");
        rows = userMapper.insert(user);
        assertTrue(rows == 1);
        
        //---------insert batch objects-----------//
        TestUser[] users = new TestUser[] { user };
        int rows2 = userMapper.insertUserArray(users);
        assertTrue(rows2 == 1);
        //        userMapper.insertABC(users);
        //        
        List<TestUser> users2 = new ArrayList<>();
        users2.add(user);
        int rows3 = userMapper.insertUserList(users2);
        assertTrue(rows3 == 1);
        
    }
    
    @Test
    @Transactional
    public void testUpdate()
    {
        TestUser user = new TestUser();
        user.setId(15l);
        user.setUserName("abc");
        int rows = userMapper.updateUser(user);
        assertTrue(rows == 1);
        rows = userMapper.updateSingle(user);
        assertTrue(rows == 1);
        
        TestUser user2 = new TestUser();
        user2.setUserName("cde");
        int rows2 = userMapper.update("abc", user2);
        assertTrue(rows2 == 1);
        
        TestUser user3 = new TestUser();
        user3.setUserName("cde");
        TestUserCriterias criterias = new TestUserCriterias();
        criterias.setIds(new Long[] { 15l, 16l });
        int rows3 = userMapper.updateUsers(user3, criterias);
        assertTrue(rows3 == 2);
        //        TestUser entity = userMapper.getById(1L);
        //        assertNotNull("selected entity can not be null!", entity);
        //        
        //        TestUser newUser = new TestUser(entity.getId());
        //        newUser.setUserName("my_name");
        //        int rows = userMapper.update(newUser);
        //        
        //        assertTrue("Update entity not success!", rows == 1);
    }
    
    @Transactional
    public void testGet()
    {
        TestUser entity = userMapper.getById(17L);
        assertNotNull(entity);
        
        List<TestUser> select = userMapper.select(18l, "2");
        assertNotNull(select);
        
        TestUserCriterias criterias = new TestUserCriterias();
        criterias.setUserid(15l);
        List<TestUser> select2 = userMapper.selectByCriterias(criterias);
        assertNotNull(select2);
        
        TestUserCriterias criterias2 = new TestUserCriterias();
        criterias2.setUserid(15l);
        //        criterias2.setUsername("2");
        criterias2.setIds(new Long[] { 15l, 16l });
        List<TestUser> select3 = userMapper
                .selectByCriteriasAndUsername(criterias2, "2");
        assertNotNull(select3);
        
        List<TestUser> select4 = userMapper
                .selectByIds(new Long[] { 15l, 16l });
        assertNotNull(select4);
        
        TestUserCriterias criterias5 = new TestUserCriterias();
        criterias5.setIds(new Long[] { 15l, 16l });
        List<TestUser> select5 = userMapper.selectByCriterias(criterias5);
        assertNotNull(select5);
    }
    
    @Transactional
    public void testDelete()
    {
        int rows = userMapper.delete(17l);
        assertTrue(rows == 1);
        
        rows = userMapper.deleteByIdAndName(15l, "2");
        assertTrue(rows == 0);
        rows = userMapper.deleteByIdAndName(15l, "makersoft");
        assertTrue(rows == 1);
        
        TestUserCriterias criterias = new TestUserCriterias();
        criterias.setUserid(15l);
        rows = userMapper.deleteByCriteriasAndUserName(criterias, "2");
        assertTrue("Delete operation could not success!", rows > 1);
        
        TestUserCriterias criterias2 = new TestUserCriterias();
        criterias2.setIds(new Long[] { 15l, 16l });
        rows = userMapper.deleteByCriterias(criterias2);
        assertTrue(rows == 0);
        
    }
    
}
