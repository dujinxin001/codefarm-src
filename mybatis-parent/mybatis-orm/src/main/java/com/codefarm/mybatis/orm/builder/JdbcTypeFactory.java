package com.codefarm.mybatis.orm.builder;

import java.math.BigDecimal;
import java.security.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.type.JdbcType;

public class JdbcTypeFactory
{
    public static Map<Class<?>, JdbcType> javaToJdbc = new HashMap<>();
    static
    {
        javaToJdbc.put(Long.class, JdbcType.BIGINT);
        javaToJdbc.put(Boolean.class, JdbcType.BIT);
        javaToJdbc.put(byte[].class, JdbcType.BLOB);
        javaToJdbc.put(String.class, JdbcType.VARCHAR);
        javaToJdbc.put(Date.class, JdbcType.DATE);
        javaToJdbc.put(BigDecimal.class, JdbcType.DECIMAL);
        javaToJdbc.put(Double.class, JdbcType.DOUBLE);
        javaToJdbc.put(Integer.class, JdbcType.INTEGER);
        javaToJdbc.put(Float.class, JdbcType.REAL);
        javaToJdbc.put(Timestamp.class, JdbcType.TIMESTAMP);
    }
    
    public static JdbcType getJdbcType(Class<?> javaType)
    {
        return javaToJdbc.get(javaType);
    }
    
    class JavaJdbcType
    {
        private Class<?> javaType;
        
        private JdbcType jdbcType;
        
        public JavaJdbcType(Class<?> javaType, JdbcType jdbcType)
        {
            super();
            this.javaType = javaType;
            this.jdbcType = jdbcType;
        }
        
        public Class<?> getJavaType()
        {
            return javaType;
        }
        
        public void setJavaType(Class<?> javaType)
        {
            this.javaType = javaType;
        }
        
        public JdbcType getJdbcType()
        {
            return jdbcType;
        }
        
        public void setJdbcType(JdbcType jdbcType)
        {
            this.jdbcType = jdbcType;
        }
        
    }
    
    public static void main(String... args)
    {
        System.out.println(JdbcTypeFactory.getJdbcType(String.class));
    }
}
