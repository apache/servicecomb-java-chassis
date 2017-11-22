/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.metrics;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.InitializingBean;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixEventType;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.monitor.AbstractMonitor;
import com.netflix.servo.monitor.BasicCompositeMonitor;
import com.netflix.servo.monitor.Gauge;
import com.netflix.servo.monitor.Informational;
import com.netflix.servo.monitor.Monitor;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.CounterToRateMetricTransform;
import com.netflix.servo.publish.FileMetricObserver;
import com.netflix.servo.publish.MemoryMetricObserver;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;

import io.servicecomb.foundation.metrics.performance.MetricsDataMonitor;
import io.servicecomb.foundation.metrics.performance.QueueMetricsData;
import rx.functions.Func0;

/**
 * Implementation of metrics preparation and servo registry.
 */
public class MetricsServoRegistry implements InitializingBean {

  protected static ThreadLocal<MetricsDataMonitor> LOCAL_METRICS_MAP = new ThreadLocal<>();

  private static final String METRICS_POLL_TIME = "cse.metrics.polltime";

  private static final String FILENAME = "cse.metrics.file.name";

  private static final String FILEPATH = "cse.metrics.file.path";

  private MetricsDataMonitor localMetrics = new MetricsDataMonitor();

  protected static Vector<MetricsDataMonitor> metricsList = new Vector<>();

  private MemoryMetricObserver memoryObserver = null;

  public MemoryMetricObserver getMemoryObserver() {
    return memoryObserver;
  }

  /*
     * Added getter for unit test of local metrics.
     *
     * @return Local metric reference
     */
  public MetricsDataMonitor getLocalMetrics() {
    return localMetrics;
  }

  /**
   * Get or create local metrics.
   * @return MetricsDataMonitor
   */
  public static MetricsDataMonitor getOrCreateLocalMetrics() {
    MetricsDataMonitor metricsDataMonitor = LOCAL_METRICS_MAP.get();
    if (metricsDataMonitor == null) {
      metricsDataMonitor = new MetricsDataMonitor();
      LOCAL_METRICS_MAP.set(metricsDataMonitor);
      metricsList.add(metricsDataMonitor);
    }
    return metricsDataMonitor;
  }

  /**
   * Get the initial metrics and register with servo.
   */
  public void initMetricsPublishing() {

    /* list of monitors */
    List<Monitor<?>> monitors = getMetricsMonitors();
    MonitorConfig commandMetricsConfig = MonitorConfig.builder("metrics").build();
    BasicCompositeMonitor commandMetricsMonitor = new BasicCompositeMonitor(commandMetricsConfig, monitors);
    DefaultMonitorRegistry.getInstance().register(commandMetricsMonitor);
    PollScheduler scheduler = PollScheduler.getInstance();
    if (!scheduler.isStarted()) {
      scheduler.start();
    }

    int metricPoll = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_POLL_TIME, 60).get();
    String fileName = DynamicPropertyFactory.getInstance().getStringProperty(FILENAME, "metrics").get();
    String filePath = DynamicPropertyFactory.getInstance().getStringProperty(FILEPATH, ".").get();
    MetricObserver fileObserver = new FileMetricObserver(fileName, new File(filePath));
    MetricObserver fileTransform = new CounterToRateMetricTransform(fileObserver, metricPoll, TimeUnit.SECONDS);

    this.memoryObserver = new MemoryMetricObserver("default", 1);
    MetricObserver memoryTransform = new CounterToRateMetricTransform(memoryObserver, metricPoll, TimeUnit.SECONDS);

    PollRunnable fileTask = new PollRunnable(new MonitorRegistryMetricPoller(), BasicMetricFilter.MATCH_ALL,
        fileTransform);
    PollRunnable memoryTask = new PollRunnable(new MonitorRegistryMetricPoller(), BasicMetricFilter.MATCH_ALL,
        memoryTransform);

    scheduler.addPoller(fileTask, metricPoll, TimeUnit.SECONDS);
    scheduler.addPoller(memoryTask, metricPoll, TimeUnit.SECONDS);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    initMetricsPublishing();
  }

  /**
   * Get instance level total requests by comparing the last saved data.
   */
  protected final Func0<Number> getTotalReqProvider = new Func0<Number>() {
    @Override
    public Number call() {
      Long totalReqProvider = 0L;
      for (MetricsDataMonitor metricsDataMonitor : metricsList) {
        totalReqProvider += metricsDataMonitor.getTotalReqProvider();
      }
      Long metricValue = totalReqProvider - localMetrics.getTotalReqProvider();
      localMetrics.setTotalReqProvider(totalReqProvider);
      return metricValue;
    }
  };

  /**
   * Get instance level total failed requests by comparing the last saved data.
   */
  protected final Func0<Number> getTotalFailedReqProvider = new Func0<Number>() {
    @Override
    public Number call() {
      Long totalFailedReqProvider = 0L;
      for (MetricsDataMonitor metricsDataMonitor : metricsList) {
        totalFailedReqProvider += metricsDataMonitor.getTotalFailReqProvider();
      }
      Long metricValue = totalFailedReqProvider - localMetrics.getTotalFailReqProvider();
      localMetrics.setTotalFailReqProvider(totalFailedReqProvider);
      return metricValue;
    }
  };

  /**
   * Get instance level total failed requests by comparing the last saved data.
   */
  protected final Func0<Number> getTotalReqConsumer = new Func0<Number>() {
    @Override
    public Number call() {
      Long totalReqConsumer = 0L;
      for (MetricsDataMonitor metricsDataMonitor : metricsList) {
        totalReqConsumer += metricsDataMonitor.getTotalReqConsumer();
      }
      Long metricValue = totalReqConsumer - localMetrics.getTotalReqConsumer();
      localMetrics.setTotalReqConsumer(totalReqConsumer);
      return metricValue;
    }
  };

  /**
   * Get instance level total failed requests by comparing the last saved data.
   */
  protected final Func0<Number> getFailedTotalReqConsumer = new Func0<Number>() {
    @Override
    public Number call() {
      Long totalFailedReqConsumer = 0L;
      for (MetricsDataMonitor metricsDataMonitor : metricsList) {
        totalFailedReqConsumer += metricsDataMonitor.getTotalFailReqConsumer();
      }
      Long metricValue = totalFailedReqConsumer - localMetrics.getTotalFailReqConsumer();
      localMetrics.setTotalFailReqConsumer(totalFailedReqConsumer);
      return metricValue;
    }
  };

  /**
   * Get operational level total request and total failed requests by comparing the
   * last saved data.
   */
  protected final Func0<String> getTotalReqProdOperLevel = new Func0<String>() {
    @Override
    public String call() {
      Map<String, Long> totalMap = new HashMap<String, Long>();
      Map<String, Long> oldMap = localMetrics.operMetricsTotalReq;
      Map<String, Long> metricMap = new HashMap<String, Long>();
      for (MetricsDataMonitor metricsDataMonitor : metricsList) {
        Collection<String> keySet = metricsDataMonitor.operMetricsTotalReq.keySet();
        for (String key : keySet) {
          Long value = totalMap.get(key);
          if (null == value) {
            totalMap.put(key, metricsDataMonitor.getOperMetTotalReq(key));
          } else {
            totalMap.put(key, metricsDataMonitor.getOperMetTotalReq(key) + value);
          }
        }
      }
      Collection<String> keySet = totalMap.keySet();
      for (String key : keySet) {
        if (oldMap.containsKey(key)) {
          metricMap.put(key, totalMap.get(key) - oldMap.get(key));
        } else {
          metricMap.put(key, totalMap.get(key));
        }
      }
      localMetrics.operMetricsTotalReq.putAll(totalMap);
      return metricMap.toString();
    }
  };

  /**
   * Get operational level total request and total failed requets by comparing the
   * last saved data.
   */
  protected final Func0<String> getTotalReqFailProdOperLevel = new Func0<String>() {
    @Override
    public String call() {
      Map<String, Long> totalMap = new HashMap<String, Long>();
      Map<String, Long> oldMap = localMetrics.operMetricsTotalFailReq;
      Map<String, Long> metricMap = new HashMap<String, Long>();
      for (MetricsDataMonitor metricsDataMonitor : metricsList) {
        Collection<String> keySet = metricsDataMonitor.operMetricsTotalFailReq.keySet();
        for (String key : keySet) {
          Long value = totalMap.get(key);
          if (null == value) {
            totalMap.put(key, metricsDataMonitor.getOperMetTotalFailReq(key));
          } else {
            totalMap.put(key, metricsDataMonitor.getOperMetTotalFailReq(key) + value);
          }
        }
      }
      Collection<String> keySet = totalMap.keySet();
      for (String key : keySet) {
        if (oldMap.containsKey(key)) {
          metricMap.put(key, totalMap.get(key) - oldMap.get(key));
        } else {
          metricMap.put(key, totalMap.get(key));
        }
      }
      localMetrics.operMetricsTotalFailReq.putAll(totalMap);
      return metricMap.toString();
    }
  };

  /**
   * Get operational level/instance level queue related metrics by comparing the
   * last saved data.
   */
  protected final Func0<String> getQueueMetrics = new Func0<String>() {
    @Override
    public String call() {
      Map<String, QueueMetricsData> totalMap = new HashMap<String, QueueMetricsData>();

      for (MetricsDataMonitor metricsDataMonitor : metricsList) {
        Collection<String> keySet = metricsDataMonitor.getQueueMetrics().keySet();
        for (String key : keySet) {
          QueueMetricsData value = totalMap.get(key);
          if (null == value) {
            totalMap.put(key, metricsDataMonitor.getQueueMetrics().get(key));
          } else {
            QueueMetricsData newValue = metricsDataMonitor.getQueueMetrics().get(key);
            QueueMetricsData totalValue = new QueueMetricsData();
            totalValue.setCountInQueue(newValue.getCountInQueue() + value.getCountInQueue());
            totalValue.setTotalTime(newValue.getTotalTime() + value.getTotalTime());
            totalValue.setTotalCount(newValue.getTotalCount() + value.getTotalCount());
            totalValue
                .setTotalServExecutionTime(newValue.getTotalServExecutionTime() + value.getTotalServExecutionTime());
            totalValue
                .setTotalServExecutionCount(newValue.getTotalServExecutionCount() + value.getTotalServExecutionCount());
            if ((value.getMinLifeTimeInQueue() <= 0)
                || (newValue.getMinLifeTimeInQueue() < value.getMinLifeTimeInQueue())) {
              totalValue.setMinLifeTimeInQueue(newValue.getMinLifeTimeInQueue());
            }
            newValue.resetMinLifeTimeInQueue();
            if ((value.getMaxLifeTimeInQueue() <= 0)
                || (newValue.getMaxLifeTimeInQueue() > value.getMaxLifeTimeInQueue())) {
              totalValue.setMaxLifeTimeInQueue(newValue.getMaxLifeTimeInQueue());
            }
            newValue.resetMaxLifeTimeInQueue();
            totalMap.put(key, totalValue);
          }
        }
      }

      Map<String, QueueMetricsData> oldMap = localMetrics.getQueueMetrics();
      Map<String, QueueMetricsData> metricMap = new HashMap<String, QueueMetricsData>();
      Map<String, String> result = new HashMap<>();
      Map<String, String> resultInstancePublishMap = new HashMap<>();

      QueueMetricsData totalValueInstance = new QueueMetricsData();

      Collection<String> keySet = totalMap.keySet();
      Map<String, String> resultMap;

      for (String key : keySet) {
        resultMap = new HashMap<>();
        if (oldMap.containsKey(key)) {
          QueueMetricsData newValue = new QueueMetricsData();
          QueueMetricsData totalValue = totalMap.get(key);
          QueueMetricsData oldValue = oldMap.get(key);
          newValue.setCountInQueue(totalValue.getCountInQueue());
          newValue.setTotalTime(totalValue.getTotalTime() - oldValue.getTotalTime());
          newValue.setTotalCount(totalValue.getTotalCount() - oldValue.getTotalCount());
          newValue
              .setTotalServExecutionTime(totalValue.getTotalServExecutionTime() - oldValue.getTotalServExecutionTime());
          newValue.setTotalServExecutionCount(
              totalValue.getTotalServExecutionCount() - oldValue.getTotalServExecutionCount());
          newValue.setMinLifeTimeInQueue(totalValue.getMinLifeTimeInQueue());
          newValue.setMaxLifeTimeInQueue(totalValue.getMaxLifeTimeInQueue());
          metricMap.put(key, newValue);
        } else {
          metricMap.put(key, totalMap.get(key));
        }

        resultMap.put("countInQueue", metricMap.get(key).getCountInQueue().toString());

        long count = metricMap.get(key).getTotalCount();
        double avgTimeInQueue = 0;
        if (count > 0) {
          avgTimeInQueue = metricMap.get(key).getTotalTime() / count;
        }
        resultMap.put("AverageTimeInQueue", String.valueOf(avgTimeInQueue));
        long countService = metricMap.get(key).getTotalServExecutionCount();
        double avgServiceTimeInQueue = 0;
        if (countService > 0) {
          avgServiceTimeInQueue = metricMap.get(key).getTotalServExecutionTime() / countService;
        }
        resultMap.put("AverageServiceExecutionTime", String.valueOf(avgServiceTimeInQueue));
        resultMap.put("MinLifeTimeInQueue", metricMap.get(key).getMinLifeTimeInQueue().toString());
        resultMap.put("MaxLifeTimeInQueue", metricMap.get(key).getMaxLifeTimeInQueue().toString());

        result.put(key, resultMap.toString());

        //get the all values for instance level.
        totalValueInstance.setCountInQueue(metricMap.get(key).getCountInQueue());
        totalValueInstance.setTotalTime(totalValueInstance.getTotalTime() + metricMap.get(key).getTotalTime());
        totalValueInstance.setTotalCount(totalValueInstance.getTotalCount() + metricMap.get(key).getTotalCount());
        totalValueInstance
            .setTotalServExecutionTime(
                totalValueInstance.getTotalServExecutionTime() + metricMap.get(key).getTotalServExecutionTime());
        totalValueInstance
            .setTotalServExecutionCount(
                totalValueInstance.getTotalServExecutionCount() + metricMap.get(key).getTotalServExecutionCount());

        if (totalValueInstance.getMinLifeTimeInQueue() <= 0
            || metricMap.get(key).getMinLifeTimeInQueue() < totalValueInstance.getMinLifeTimeInQueue()) {
          totalValueInstance.setMinLifeTimeInQueue(metricMap.get(key).getMinLifeTimeInQueue());
        }
        if (totalValueInstance.getMaxLifeTimeInQueue() <= 0
            || totalMap.get(key).getMaxLifeTimeInQueue() > totalValueInstance.getMaxLifeTimeInQueue()) {
          totalValueInstance.setMaxLifeTimeInQueue(metricMap.get(key).getMaxLifeTimeInQueue());
        }

        localMetrics.setQueueMetrics(new ConcurrentHashMap<>(totalMap));
      }

      //prepare the result map for instance level.
      resultInstancePublishMap.put("countInQueue", totalValueInstance.getCountInQueue().toString());
      long countInst = totalValueInstance.getTotalCount();
      double avgTimeInQueueIns = 0;
      if (countInst > 0) {
        avgTimeInQueueIns = totalValueInstance.getTotalTime() / countInst;
      }
      resultInstancePublishMap.put("AverageTimeInQueue", String.valueOf(avgTimeInQueueIns));
      long countServiceInst = totalValueInstance.getTotalServExecutionCount();
      double avgServiceTimeInQueueInst = 0;
      if (countServiceInst > 0) {
        avgServiceTimeInQueueInst = totalValueInstance.getTotalServExecutionTime() / countServiceInst;
      }
      resultInstancePublishMap.put("AverageServiceExecutionTime", String.valueOf(avgServiceTimeInQueueInst));
      resultInstancePublishMap.put("MinLifeTimeInQueue", totalValueInstance.getMinLifeTimeInQueue().toString());
      resultInstancePublishMap.put("MaxLifeTimeInQueue", totalValueInstance.getMaxLifeTimeInQueue().toString());
      result.put("InstanceLevel", resultInstancePublishMap.toString());

      return result.toString();
    }
  };

  /**
   * Get CPU and memory information metrics.
   */
  protected final Func0<String> getCpuAndMemory = new Func0<String>() {
    @Override
    public String call() {
      Map<String, String> memoryMap = new HashMap<>();
      OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();
      double cpu = osMxBean.getSystemLoadAverage();
      memoryMap.put("CPU System Load", String.valueOf(cpu));

      ThreadMXBean threadmxBean = ManagementFactory.getThreadMXBean();
      int threadCount = threadmxBean.getThreadCount();
      memoryMap.put("CPU Current Running Threads", String.valueOf(threadCount));

      MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
      MemoryUsage memHeapUsage = memBean.getHeapMemoryUsage();
      MemoryUsage nonHeapUsage = memBean.getNonHeapMemoryUsage();
      memoryMap.put("heapInit", String.valueOf(memHeapUsage.getInit()));
      memoryMap.put("heapMax", String.valueOf(memHeapUsage.getMax()));
      memoryMap.put("heapCommit", String.valueOf(memHeapUsage.getCommitted()));
      memoryMap.put("heapUsed", String.valueOf(memHeapUsage.getUsed()));
      memoryMap.put("nonHeapInit", String.valueOf(nonHeapUsage.getInit()));
      memoryMap.put("nonHeapMax", String.valueOf(nonHeapUsage.getMax()));
      memoryMap.put("nonHeapCommit", String.valueOf(nonHeapUsage.getCommitted()));
      memoryMap.put("nonHeapUsed", String.valueOf(nonHeapUsage.getUsed()));
      return memoryMap.toString();
    }
  };

  /**
   * Get TPS and latency for operational and instance level from hystrix.
   */
  protected final Func0<String> getTpsAndLatency = new Func0<String>() {
    @Override
    public String call() {
      Map<String, String> tpsAndLatencyMap = new HashMap<>();
      Collection<HystrixCommandMetrics> instances = HystrixCommandMetrics.getInstances();

      long insTotalTps = 0;
      long insTotalLatency = 0;
      long cumulativeTotalCount = 0;

      for (HystrixCommandMetrics instance : instances) {
        long successCount = instance.getRollingCount(HystrixEventType.SUCCESS);
        long failureCount = instance.getRollingCount(HystrixEventType.FAILURE);
        int operLatency = instance.getExecutionTimeMean();
        long totalCallCount = successCount + failureCount;
        cumulativeTotalCount += totalCallCount;
        int windowTime = instance.getProperties().metricsRollingStatisticalWindowInMilliseconds().get() / 1000;
        double qpsVal = (double) (totalCallCount) / windowTime;
        BigDecimal bigDecimal = new BigDecimal(qpsVal);
        BigDecimal bigDecimalVal = bigDecimal.setScale(1, RoundingMode.HALF_DOWN);
        Double tpsOper = bigDecimalVal.doubleValue();
        tpsAndLatencyMap.put("TPS-" + instance.getCommandKey().name(), String.valueOf(tpsOper));
        tpsAndLatencyMap.put("Latency-" + instance.getCommandKey().name(), String.valueOf(operLatency));
        insTotalTps += tpsOper;
        insTotalLatency += operLatency;
      }

      double instanceLatency = (double) (insTotalLatency) / cumulativeTotalCount;

      tpsAndLatencyMap.put("TPS Instance_Level", String.valueOf(insTotalTps));
      tpsAndLatencyMap.put("Latency Instance_Level", String.valueOf(instanceLatency));

      return tpsAndLatencyMap.toString();
    }
  };

  /**
   * Implementation of request metrics with using servo guage metric type.
   */
  protected abstract class GaugeMetric extends AbstractMonitor<Number> implements Gauge<Number> {

    public GaugeMetric(MonitorConfig config) {
      super(config.withAdditionalTag(DataSourceType.GAUGE));
    }

    @Override
    public Number getValue(int n) {
      return getValue();
    }

    @Override
    public abstract Number getValue();
  }

  /**
   * Implementation of queue average metrics with using servo information metric
   * type.
   */
  protected abstract class InformationalMetric extends AbstractMonitor<String> implements Informational {
    public InformationalMetric(MonitorConfig config) {
      super(config.withAdditionalTag(DataSourceType.INFORMATIONAL));
    }

    @Override
    public String getValue(int n) {
      return getValue();
    }

    @Override
    public abstract String getValue();
  }

  /**
   * Get the total requests and failed requests for instance level.
   *
   * @param metricsName Name of the metrics
   * @param instance  object of latest metrics
   * @param fieldName metric field
   * @param metricToEvaluate observable method to be called for preparation of metrics.
   * @return Guage metrics
   */
  protected Monitor<Number> getRequestValuesGaugeMonitor(final String metricsName,
      final Func0<Number> metricToEvaluate) {
    return new GaugeMetric(MonitorConfig.builder(metricsName).build()) {

      @Override
      public Number getValue() {
        return metricToEvaluate.call();
      }
    };
  }

  /**
   * Get the total requests and failed requests for each producer.
   *
   * @param metricsName  Name of the metrics
   * @param instance object of latest metrics
   * @param fieldName metric field
   * @param metricToEvaluate observable method to be called for preparation of metrics.
   * @return Guage metrics
   */
  protected Monitor<String> getInfoMetricsOperationLevel(final String metricsName,
      final Func0<String> metricToEvaluate) {
    return new InformationalMetric(MonitorConfig.builder(metricsName).build()) {
      @Override
      public String getValue() {
        return metricToEvaluate.call();
      }
    };
  }

  /**
   * Get the total requests and failed requests for each producer.
   *
   * @param metricsName Name of the metrics
   * @param instance object of latest metrics
   * @param fieldName metric field
   * @param metricToEvaluate observable method to be called for preparation of metrics.
   * @return Guage metrics
   */
  protected Monitor<String> getInfoMetricsOperationalAndInstance(final String name,
      final Func0<String> metricToEvaluate) {
    return new InformationalMetric(MonitorConfig.builder(name).build()) {
      @Override
      public String getValue() {
        return metricToEvaluate.call();
      }
    };
  }

  /**
   * Prepare the initial metrics.
   *
   * @return List of monitors
   */
  private List<Monitor<?>> getMetricsMonitors() {

    List<Monitor<?>> monitors = new ArrayList<Monitor<?>>();
    monitors.add(getRequestValuesGaugeMonitor("TotalRequestsPerProvider INSTANCE_LEVEL",
        getTotalReqProvider));

    monitors.add(getRequestValuesGaugeMonitor("TotalFailedRequestsPerProvider INSTANCE_LEVEL",
        getTotalFailedReqProvider));

    monitors.add(getRequestValuesGaugeMonitor("TotalRequestsPerConsumer INSTANCE_LEVEL",
        getTotalReqConsumer));

    monitors.add(getRequestValuesGaugeMonitor("TotalFailRequestsPerConsumer INSTANCE_LEVEL",
        getFailedTotalReqConsumer));

    monitors.add(getInfoMetricsOperationLevel("TotalRequestProvider OPERATIONAL_LEVEL",
        getTotalReqProdOperLevel));

    monitors.add(getInfoMetricsOperationLevel("TotalFailedRequestProvider OPERATIONAL_LEVEL",
        getTotalReqFailProdOperLevel));

    monitors.add(getInfoMetricsOperationalAndInstance("RequestQueueRelated", getQueueMetrics));

    monitors.add(getInfoMetricsOperationalAndInstance("TPS and Latency", getTpsAndLatency));

    monitors.add(getInfoMetricsOperationalAndInstance("CPU and Memory", getCpuAndMemory));

    return monitors;
  }
}
