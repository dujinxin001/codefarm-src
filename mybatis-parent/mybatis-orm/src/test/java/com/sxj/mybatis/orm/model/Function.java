package com.sxj.mybatis.orm.model;

import java.io.Serializable;

import com.sxj.mybatis.orm.annotations.Column;
import com.sxj.mybatis.orm.annotations.Entity;
import com.sxj.mybatis.orm.annotations.GeneratedValue;
import com.sxj.mybatis.orm.annotations.GenerationType;
import com.sxj.mybatis.orm.annotations.Id;
import com.sxj.mybatis.orm.annotations.Sn;
import com.sxj.mybatis.orm.annotations.Table;
import com.sxj.mybatis.orm.keygen.SnStub;
import com.sxj.mybatis.orm.mapper.FunctionMapper;

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
