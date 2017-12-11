package io.servicecomb.metrics.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.metrics.core.metric.BackgroundMetric;
import io.servicecomb.metrics.core.metric.BasicTimerMetric;
import io.servicecomb.metrics.core.metric.DefaultMetricFactory;
import io.servicecomb.metrics.core.metric.Metric;
import io.servicecomb.metrics.core.metric.MetricFactory;

public class TestDefaultMetricFactory {

  @Test
  public void testMetricFactory() throws InterruptedException {

    System.getProperties().setProperty("servo.pollers", "1000");

    MetricFactory factory = new DefaultMetricFactory();

    Map<String, Number> backgroundData = new HashMap<>();
    backgroundData.put("A", 100);
    backgroundData.put("B", 200);

    Metric counter = factory.createCounter("counter");
    Metric timer = factory.createTimer("timer");
    Metric background = factory.createBackground("counter", () -> backgroundData, 100);
    Metric custom = factory.createCustom("custom", () -> 1000);
    Metric doubleGauge = factory.createDoubleGauge("double");
    Metric longGauge = factory.createLongGauge("long");

    counter.update(1);
    doubleGauge.update(2);
    longGauge.update(3);
    timer.update(TimeUnit.MILLISECONDS.toNanos(1000));
    timer.update(TimeUnit.MILLISECONDS.toNanos(2000));
    timer.update(TimeUnit.MILLISECONDS.toNanos(3000));

    Thread.sleep(1000);

    Map<String, Number> result = timer.getAll();
    Assert.assertTrue(result.get("timer." + BasicTimerMetric.MIN).doubleValue() == 1000);
    Assert.assertTrue(result.get("timer." + BasicTimerMetric.MAX).doubleValue() == 3000);
    Assert.assertTrue(result.get("timer." + BasicTimerMetric.AVERAGE).doubleValue() == 2000);

    Assert.assertTrue(counter.getAll().get("counter").longValue() == 1);
    Assert.assertTrue(background.getAll().get("A").longValue() == 100);
    Assert.assertTrue(background.getAll().get("B").longValue() == 200);
    Assert.assertTrue(custom.getAll().get("custom").longValue() == 1000);
    Assert.assertTrue(doubleGauge.getAll().get("double").doubleValue() == 2);
    Assert.assertTrue(longGauge.getAll().get("long").longValue() == 3);

    Assert.assertTrue(background.get("A").longValue() == 100);
    Assert.assertTrue(((BackgroundMetric)background).getAllWithFilter("A").get("A").longValue() == 100);

  }
}
