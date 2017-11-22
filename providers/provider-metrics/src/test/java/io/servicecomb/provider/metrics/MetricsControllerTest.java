package io.servicecomb.provider.metrics;

import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.servo.publish.PollScheduler;

import io.servicecomb.foundation.metrics.MetricsServoRegistry;
import io.servicecomb.foundation.metrics.performance.MetricsDataMonitor;

public class MetricsControllerTest {

  MetricsDataMonitor metricsDataMonitor = null;

  MetricsDataMonitor localData = null;

  MetricsServoRegistry metricsRegistry = null;

  private final ObjectMapper mapper = new ObjectMapper();

  @Before
  public void setUp() throws Exception {
    metricsRegistry = new MetricsServoRegistry();
    metricsRegistry.initMetricsPublishing();
    localData = metricsRegistry.getLocalMetrics();
    metricsDataMonitor = MetricsServoRegistry.getOrCreateLocalMetrics();
  }

  @After
  public void tearDown() throws Exception {
    PollScheduler.getInstance().stop();
    metricsRegistry = null;
    localData = null;
    metricsDataMonitor = null;
  }


  @Test
  public void testMetricsRestPublish() throws Exception {
    metricsDataMonitor.incrementTotalReqProvider();
    metricsDataMonitor.incrementTotalFailReqProvider();
    metricsDataMonitor.incrementTotalReqConsumer();
    metricsDataMonitor.incrementTotalFailReqConsumer();
    metricsDataMonitor.setOperMetTotalReq("sayHi", 20L);
    metricsDataMonitor.setOperMetTotalFailReq("sayHi", 20L);
    localData = metricsRegistry.getLocalMetrics();
    localData.setOperMetTotalReq("sayHi", 10L);
    localData.setOperMetTotalFailReq("sayHi", 10L);

    metricsRegistry.afterPropertiesSet();
    Thread.sleep(3000);
    // get the metrics from local data and compare

    MetricsController controller = new MetricsController(metricsRegistry);

    String metricsJson = controller.metrics();

    Map<String, String> metrics = mapper.readValue(metricsJson, Map.class);

    Assert.assertTrue(metrics.containsKey("TPS and Latency"));
    Assert.assertTrue(metrics.containsKey("RequestQueueRelated"));
    Assert.assertTrue(metrics.containsKey("CPU and Memory"));
  }
}
