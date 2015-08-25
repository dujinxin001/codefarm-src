package com.sxj.mybatis.orm.mapper;

import java.util.List;

import com.sxj.mybatis.orm.annotations.BatchDelete;
import com.sxj.mybatis.orm.annotations.BatchInsert;
import com.sxj.mybatis.orm.annotations.BatchUpdate;
import com.sxj.mybatis.orm.annotations.Get;
import com.sxj.mybatis.orm.annotations.Insert;
import com.sxj.mybatis.orm.annotations.MultiGet;
import com.sxj.mybatis.orm.model.Function;

public interface FunctionMapper
{
    @Insert
    int insert(Function function);
    
    @Get
    Function getFunction(String functionId);
    
    void batchInsert(List<Function> functions);
    
    @BatchInsert
    void batchInsert(Function[] functions);
    
    @BatchDelete
    void batchDelete(String[] functionIds);
    
    void batchDelete(List<String> functionIds);
    
    @BatchUpdate
    void batchUpdate(List<Function> functions);
    
    void batchUpdate(Function[] functions);
    
    @MultiGet
    List<Function> multiGet(String[] functionIds);
    
    int insertDemo(Function function);
}
