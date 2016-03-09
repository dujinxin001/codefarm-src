/*
 * @(#)UserMapper.java 2013年12月23日 下午23:33:33
 *
 * Copyright (c) 2011-2013 Makersoft.org all rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 */
package com.codefarm.mybatis.orm.mapper;

import com.codefarm.mybatis.orm.annotations.Delete;
import com.codefarm.mybatis.orm.annotations.Get;
import com.codefarm.mybatis.orm.annotations.Insert;
import com.codefarm.mybatis.orm.annotations.Update;
import com.codefarm.mybatis.orm.model.TestUser;

/**
 * Class description goes here.
 */
public interface UserMapper
{
    
    @Insert
    int insertUser(TestUser user);
    
    @Insert
    int insert(TestUser user);
    
    @Delete
    int delete(TestUser user);
    
    @Update
    int update(TestUser user);
    
    @Get
    TestUser get(Long id);
    
    @Get
    TestUser getById(Long id);
}
