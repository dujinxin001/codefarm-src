package com.codefarm.mybatis.orm.model;

import java.io.Serializable;

import com.codefarm.mybatis.orm.annotations.Column;
import com.codefarm.mybatis.orm.annotations.Entity;
import com.codefarm.mybatis.orm.annotations.GeneratedValue;
import com.codefarm.mybatis.orm.annotations.GenerationType;
import com.codefarm.mybatis.orm.annotations.Id;
import com.codefarm.mybatis.orm.annotations.Sn;
import com.codefarm.mybatis.orm.annotations.Table;
import com.codefarm.mybatis.orm.keygen.SnStub;
import com.codefarm.mybatis.orm.mapper.FunctionMapper;

@Entity(mapper = FunctionMapper.class)
@Table(name = "TEST_FUNCTION")
public class Function implements Serializable, SnStub
{
    private String stubValue;
    
    @Id(column = "ID")
    @GeneratedValue(strategy = GenerationType.UUID, length = 31)
    private String functionId;
    
    @Sn(table = "T_SN", stub = "F_SN_NAME", stubValue = "function", sn = "F_SN_NUMBER", step = 1, pattern = "0000")
    @Column(name = "TITLE")
    private String functionName;
    
    public String getFunctionId()
    {
        return functionId;
    }
    
    public void setFunctionId(String functionId)
    {
        this.functionId = functionId;
    }
    
    public String getFunctionName()
    {
        return functionName;
    }
    
    public void setFunctionName(String functionName)
    {
        this.functionName = functionName;
    }
    
    @Override
    public void setStubValue(String stubValue)
    {
        this.stubValue = stubValue;
    }
    
    @Override
    public String getStubValue()
    {
        return this.stubValue;
    }
}
