/*
 * @(#)Users.java 2013年12月23日 下午23:33:33
 *
 * Copyright (c) 2011-2013 Makersoft.org all rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 */
package com.sxj.mybatis.orm.model;

import java.io.Serializable;

import com.sxj.mybatis.orm.annotations.Column;
import com.sxj.mybatis.orm.annotations.Entity;
import com.sxj.mybatis.orm.annotations.Id;
import com.sxj.mybatis.orm.annotations.Table;
import com.sxj.mybatis.orm.mapper.UserMapper;
import com.sxj.mybatis.pagination.Pagable;

/**
 * Class description goes here.
 */
@Entity(mapper = UserMapper.class)
@Table(name = "USER")
public class User extends Pagable implements Serializable
{
    
    private static final long serialVersionUID = 6275980778279891698L;
    
    @Id(column = "USER_ID", generatedKeys = false)
    private Long id;
    
    @Column(name = "USER_NAME")
    private String userName;
    
    //~~~~~~~~~~~
    public User()
    {
        
    }
    
    public User(Long id)
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
