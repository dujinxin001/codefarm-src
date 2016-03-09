/*
 * @(#)Users.java 2013年12月23日 下午23:33:33
 *
 * Copyright (c) 2011-2013 Makersoft.org all rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 */
package com.codefarm.mybatis.orm.model;

import java.io.Serializable;

import com.codefarm.mybatis.orm.annotations.Column;
import com.codefarm.mybatis.orm.annotations.Entity;
import com.codefarm.mybatis.orm.annotations.GeneratedValue;
import com.codefarm.mybatis.orm.annotations.GenerationType;
import com.codefarm.mybatis.orm.annotations.Id;
import com.codefarm.mybatis.orm.annotations.Table;
import com.codefarm.mybatis.orm.mapper.UserMapper;
import com.codefarm.mybatis.pagination.Pagable;

/**
 * Class description goes here.
 */
@Entity(mapper = UserMapper.class)
@Table(name = "testuser")
public class TestUser extends Pagable implements Serializable
{
    
    private static final long serialVersionUID = 6275980778279891698L;
    
    @Id(column = "userid", generatedKeys = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, sequence = "test_seq")
    private Long id;
    
    @Column(name = "username")
    private String userName;
    
    public TestUser()
    {
        super();
    }
    
    public TestUser(Long id)
    {
        this.id = id;
    }
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public String getUserName()
    {
        return userName;
    }
    
    public void setUserName(String userName)
    {
        this.userName = userName;
    }
    
}
