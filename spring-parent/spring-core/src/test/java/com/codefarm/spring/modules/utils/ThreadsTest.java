/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package com.codefarm.spring.modules.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codefarm.spring.modules.test.category.UnStable;
import com.codefarm.spring.modules.util.Threads;

@Category(UnStable.class)
public class ThreadsTest
{
    
    @Test
    public void gracefulShutdown() throws InterruptedException
    {
        
        Logger logger = LoggerFactory.getLogger("test");
        
        // time enough to shutdown
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Runnable task = new Task(logger, 500, 0);
        pool.execute(task);
        Threads.gracefulShutdown(pool, 1000, 1000, TimeUnit.MILLISECONDS);
        assertThat(pool.isTerminated()).isTrue();
        
        // time not enough to shutdown,call shutdownNow
        pool = Executors.newSingleThreadExecutor();
        task = new Task(logger, 1000, 0);
        pool.execute(task);
        Threads.gracefulShutdown(pool, 500, 1000, TimeUnit.MILLISECONDS);
        assertThat(pool.isTerminated()).isTrue();
        
        // self thread interrupt while calling gracefulShutdown
        
        final ExecutorService self = Executors.newSingleThreadExecutor();
        task = new Task(logger, 100000, 0);
        self.execute(task);
        
        final CountDownLatch lock = new CountDownLatch(1);
        Thread thread = new Thread(new Runnable()
        {
            
            @Override
            public void run()
            {
                lock.countDown();
                Threads.gracefulShutdown(self,
                        200000,
                        200000,
                        TimeUnit.MILLISECONDS);
            }
        });
        thread.start();
        lock.await();
        thread.interrupt();
        Threads.sleep(500);
    }
    
    @Test
    public void normalShutdown() throws InterruptedException
    {
        
        Logger logger = LoggerFactory.getLogger("test");
        
        // time not enough to shutdown,write error log.
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Runnable task = new Task(logger, 1000, 0);
        pool.execute(task);
        Threads.normalShutdown(pool, 500, TimeUnit.MILLISECONDS);
        assertThat(pool.isTerminated()).isTrue();
    }
    
    static class Task implements Runnable
    {
        private final Logger logger;
        
        private int runTime = 0;
        
        private final int sleepTime;
        
        Task(Logger logger, int sleepTime, int runTime)
        {
            this.logger = logger;
            this.sleepTime = sleepTime;
            this.runTime = runTime;
        }
        
        @Override
        public void run()
        {
            System.out.println("start task");
            if (runTime > 0)
            {
                long start = System.currentTimeMillis();
                while ((System.currentTimeMillis() - start) < runTime)
                {
                }
            }
            
            try
            {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e)
            {
                logger.warn("InterruptedException");
            }
        }
    }
}
