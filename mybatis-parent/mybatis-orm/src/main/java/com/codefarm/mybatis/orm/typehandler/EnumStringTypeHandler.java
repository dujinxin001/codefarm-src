package com.codefarm.mybatis.orm.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class EnumStringTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E>
{
    private Class<E> type;
    
    public EnumStringTypeHandler(Class<E> type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter,
            JdbcType jdbcType) throws SQLException
    {
        ps.setString(i, parameter.name());
    }
    
    @Override
    public E getNullableResult(ResultSet rs, String columnName)
            throws SQLException
    {
        String name = rs.getString(columnName);
        return Enum.valueOf(type, name);
    }
    
    @Override
    public E getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException
    {
        String name = rs.getString(columnIndex);
        if (rs.wasNull())
        {
            return null;
        }
        else
        {
            try
            {
                return Enum.valueOf(type, name);
            }
            catch (Exception ex)
            {
                throw new IllegalArgumentException("Cannot convert " + name
                        + " to " + type.getSimpleName() + " by ordinal value.",
                        ex);
            }
        }
    }
    
    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException
    {
        String name = cs.getString(columnIndex);
        if (cs.wasNull())
        {
            return null;
        }
        else
        {
            try
            {
                return Enum.valueOf(type, name);
            }
            catch (Exception ex)
            {
                throw new IllegalArgumentException("Cannot convert " + name
                        + " to " + type.getSimpleName() + " by ordinal value.",
                        ex);
            }
        }
    }
    
}
