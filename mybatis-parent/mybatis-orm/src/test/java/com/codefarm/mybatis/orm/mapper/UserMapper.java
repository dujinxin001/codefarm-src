/*
 * @(#)UserMapper.java 2013年12月23日 下午23:33:33
 *
 * Copyright (c) 2011-2013 Makersoft.org all rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 */
package com.codefarm.mybatis.orm.mapper;

import java.util.List;

import com.codefarm.mybatis.orm.annotations.Criteria;
import com.codefarm.mybatis.orm.annotations.Select;
import com.codefarm.mybatis.orm.model.TestUser;
import com.codefarm.mybatis.orm.po.TestUserCriterias;

/**
 * Class description goes here.
 */
public interface UserMapper
{
    
    //    @Insert
    //    int insertUser(TestUser user);
    //    
    //    @Insert
    //    int insert(TestUser user);
    //    
    //    @Delete
    //    int delete(@Criteria(column = "userid") Long id);
    //    
    //    @Delete
    //    int deleteByIdAndName(@Criteria(column = "userid") Long id,
    //            @Criteria(column = "username") String name);
    //    
    //    @Delete
    //    int deleteByCriterias(TestUserCriterias criterias);
    //    
    //    @Delete
    //    int deleteByCriteriasAndUserName(TestUserCriterias criterias,
    //            @Criteria(column = "username") String username);
    //    
    //    @Update
    //    int update(TestUser user);
    //    
    @Select(orderby = "userid desc")
    TestUser getById(@Criteria(column = "userid") Long id);
    
    @Select
    List<TestUser> select(@Criteria(column = "userid") Long id,
            @Criteria(column = "username") String username);
    
    @Select(orderby = "userid asc")
    List<TestUser> selectByCriterias(TestUserCriterias criterias);
    
    @Select(orderby = "userid desc")
    List<TestUser> selectByIds(@Criteria(column = "userid") Long[] ids);
    
}
