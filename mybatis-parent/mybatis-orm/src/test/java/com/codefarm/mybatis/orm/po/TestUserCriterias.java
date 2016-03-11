package com.codefarm.mybatis.orm.po;

import com.codefarm.mybatis.orm.annotations.Criteria;
import com.codefarm.mybatis.orm.annotations.Criterias;
import com.codefarm.mybatis.orm.enums.Operator;

@Criterias
public class TestUserCriterias
{
    @Criteria(column = "userid", operator = Operator.GREATER)
    private Long userid;
    
    @Criteria(column = "username")
    private String username = null;
    
    @Criteria(column = "userid")
    private Long[] ids;
    
    public Long getUserid()
    {
        return userid;
    }
    
    public void setUserid(Long userid)
    {
        this.userid = userid;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    public Long[] getIds()
    {
        return ids;
    }
    
    public void setIds(Long[] ids)
    {
        this.ids = ids;
    }
    
}
