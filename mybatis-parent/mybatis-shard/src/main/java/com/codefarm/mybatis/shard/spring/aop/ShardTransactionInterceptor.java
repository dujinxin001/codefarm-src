package com.codefarm.mybatis.shard.spring.aop;

import org.springframework.transaction.interceptor.TransactionInterceptor;

public class ShardTransactionInterceptor extends TransactionInterceptor
{
    public static boolean isReadOnly()
    {
        return currentTransactionInfo().getTransactionAttribute().isReadOnly();
    }
}
