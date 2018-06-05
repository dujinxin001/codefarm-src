/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package com.sxj.spring.modules.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.sxj.spring.modules.metrics.Timer.TimerContext;
import com.sxj.spring.modules.util.Clock.MockClock;

public class ExecutionTest
{
    
    @Test
    public void normal()
    {
        MockClock clock = new MockClock();
        Timer.clock = clock;
        Counter.clock = clock;
        Timer timer = new Timer(new Double[] { 90d });
        
        TimerContext timerContext = timer.start();
        clock.increaseTime(200);
        timerContext.stop();
        
        TimerContext timer2 = timer.start();
        clock.increaseTime(300);
        timer2.stop();
        
        TimerMetric metric = timer.calculateMetric();
        
        assertThat(metric.counterMetric.totalCount).isEqualTo(2);
        assertThat(metric.counterMetric.meanRate).isEqualTo(4);
        assertThat(metric.counterMetric.lastCount).isEqualTo(2);
        assertThat(metric.counterMetric.lastRate).isEqualTo(4);
        
        assertThat(metric.histogramMetric.min).isEqualTo(200);
        assertThat(metric.histogramMetric.mean).isEqualTo(250);
        assertThat(metric.histogramMetric.pcts.get(90d)).isEqualTo(300);
    }
}
