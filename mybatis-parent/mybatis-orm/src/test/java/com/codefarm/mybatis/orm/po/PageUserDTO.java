package com.codefarm.mybatis.orm.po;

import com.codefarm.mybatis.pagination.Pagable;

public class PageUserDTO extends Pagable
{
    private Integer id;
    
    private String name;
    
    public Integer getId()
    {
        return id;
    }
    
    public void setId(Integer id)
    {
        this.id = id;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
}
