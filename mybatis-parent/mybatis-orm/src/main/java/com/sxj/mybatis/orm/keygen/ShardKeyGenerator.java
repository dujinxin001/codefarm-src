package com.sxj.mybatis.orm.keygen;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.sxj.mybatis.dialect.Dialect;

public interface ShardKeyGenerator
{
    public void process(DataSource ds, Object parameter, Dialect dialect)
            throws SQLException;
}