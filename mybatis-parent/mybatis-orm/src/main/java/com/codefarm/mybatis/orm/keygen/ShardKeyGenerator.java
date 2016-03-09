package com.codefarm.mybatis.orm.keygen;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.codefarm.mybatis.dialect.Dialect;

public interface ShardKeyGenerator
{
    public void process(DataSource ds, Object parameter, Dialect dialect)
            throws SQLException;
}