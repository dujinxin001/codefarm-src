package com.codefarm.mybatis.orm.po;

import com.codefarm.mybatis.orm.annotations.Criteria;
import com.codefarm.mybatis.orm.annotations.Criterias;
import com.codefarm.mybatis.orm.enums.Operator;

@Criterias
public class TestUserCriterias
{
    @Criteria(column = "userid", operator = Operator.GREATER)
    private Long userid;
    
    public Long getUserid()
    {
        return userid;
    }
    
    public void setUserid(Long userid)
    {
        this.userid = userid;
    }
}
