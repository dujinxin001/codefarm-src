/*
 * @(#)UserMapper.java 2013年12月23日 下午23:33:33
 *
 * Copyright (c) 2011-2013 Makersoft.org all rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 */
package com.sxj.mybatis.orm.mapper;

import com.sxj.mybatis.orm.annotations.Delete;
import com.sxj.mybatis.orm.annotations.Get;
import com.sxj.mybatis.orm.annotations.Insert;
import com.sxj.mybatis.orm.annotations.Update;
import com.sxj.mybatis.orm.model.User;

/**
 * Class description goes here.
 */
public interface UserMapper
{
    
    @Insert
    int insertUser(User user);
    
    @Delete
    int delete(User user);
    
    @Update
    int update(User user);
    
    @Get
    User get(Long id);
}
