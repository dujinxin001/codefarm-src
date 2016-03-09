package com.codefarm.mybatis.shard;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.TransactionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import com.codefarm.mybatis.shard.transaction.ShardManagedTransactionFactory;

public class MybatisConfiguration
{
    
    private static Configuration configuration;
    
    private static ApplicationContext applicationContext;
    
    private static Resource configLocation;
    
    public static void setApplicationContext(
            ApplicationContext applicationContext)
    {
        MybatisConfiguration.applicationContext = applicationContext;
    }
    
    public static void initialize()
    {
        getConfiguration(configLocation);
    }
    
    private static Configuration getConfiguration(Resource configLocation)
    {
        return getConfiguration(configLocation, null);
    }
    
    private static Configuration getConfiguration(Resource configLocation,
            Properties configurationProperties)
    {
        try
        {
            if (configuration == null)
            {
                XMLConfigBuilder xmlConfigBuilder = null;
                Map<String, DataSource> beansOfType = applicationContext.getBeansOfType(DataSource.class);
                DataSource dataSource = beansOfType.values().iterator().next();
                TransactionFactory transactionFactory = new ShardManagedTransactionFactory();
                Environment environment = new Environment("development",
                        transactionFactory, dataSource);
                if (configLocation != null)
                {
                    xmlConfigBuilder = new XMLConfigBuilder(
                            configLocation.getInputStream(), null,
                            configurationProperties);
                    configuration = xmlConfigBuilder.parse();
                    configuration.setEnvironment(environment);
                }
                else
                {
                    configuration = new Configuration(environment);
                    configuration.setVariables(configurationProperties);
                }
            }
            configuration.setAutoMappingBehavior(AutoMappingBehavior.NONE);
        }
        catch (IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
        return configuration;
    }
    
    public static Configuration getConfiguration()
    {
        return configuration;
    }
    
    public static Resource getConfigLocation()
    {
        return configLocation;
    }
    
    public static void setConfigLocation(Resource configLocation)
    {
        MybatisConfiguration.configLocation = configLocation;
    }
    
}
