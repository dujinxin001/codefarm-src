package com.sxj.spring.modules.metrics.exporter;

import com.sxj.spring.modules.metrics.Counter;
import com.sxj.spring.modules.metrics.Histogram;
import com.sxj.spring.modules.metrics.Timer;

public interface MetricRegistryListener {

	void onCounterAdded(String name, Counter counter);

	void onHistogramAdded(String name, Histogram histogram);

	void onTimerAdded(String name, Timer timer);
}
