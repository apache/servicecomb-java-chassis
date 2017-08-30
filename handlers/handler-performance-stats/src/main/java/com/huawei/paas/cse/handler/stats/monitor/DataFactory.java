package com.huawei.paas.cse.handler.stats.monitor;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.paas.foundation.auth.AuthHeaderUtils;
import com.huawei.paas.foundation.auth.HttpClientFactory;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.hystrix.HystrixCommandMetrics;

import io.netty.util.concurrent.DefaultThreadFactory;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.vertx.core.json.Json;

/**
 * 数据生成工厂，启动一个线程池，定时发送数据
 * @author w00293972
 */
class DataFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataFactory.class);
    private static final int CORE_SIZE = 1;
    private static final int SLEEP_MINI_TIME = 1;
    private static final int SLEEP_MAX_TIME = 10;
    private static final int DELAY_TIME_MILLISECONDS = 1000;

    private static final String SSL_KEY = "mc.consumer";
    private boolean hasStart = false;
    private boolean firestError = true;

    private static DataFactory INSTANCE = new DataFactory();

    private ScheduledExecutorService executorService = null;

    private DataFactory() {
        ThreadFactory tf = new DefaultThreadFactory("cse-monitor-datafactory");
        executorService = Executors.newScheduledThreadPool(CORE_SIZE, tf);
    }

    public static DataFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 开启数据上报
     */
    void start() {
        if (!hasStart) {
            LOGGER.info("Start collect monitor data, send to Monitor Sender!");
            executorService.scheduleWithFixedDelay(() -> {
                int elapsedTime = sendData(getData());
                delayTime(elapsedTime);
            }, DELAY_TIME_MILLISECONDS, DELAY_TIME_MILLISECONDS, TimeUnit.MILLISECONDS);
            hasStart = true;
        }
    }

    private void delayTime(int elapsedTime) {
        int sleepTime = elapsedTime / 1000;
        try {
            if (sleepTime < SLEEP_MINI_TIME) {
                return;
            }
            if (sleepTime > SLEEP_MAX_TIME) {
                sleepTime = SLEEP_MAX_TIME;
            }
            Thread.sleep(sleepTime * 1000);
        } catch (InterruptedException e) {
            LOGGER.error("Sleep delay time error");
        }
    }

    /**
     * 发送数据
     * @param monitorData
     */
    int sendData(MonitorData monitorData) {
        if (!getEnableMonitor()) {
            return 0;
        }
        HttpClient monitorClient = HttpClientFactory.getOrCreateHttpClient(SSL_KEY);
        HttpPost httpPost = new HttpPost(getServerUrl() + "/csemonitor/v1/metric?service=" + monitorData.getName());
        httpPost.addHeader("Content-Type", "application/json");
        Map<String, String> akskHeaders = AuthHeaderUtils.getInstance().genAuthHeaders();
        for (Map.Entry<String, String> header : akskHeaders.entrySet()) {
            httpPost.setHeader(header.getKey(), header.getValue());
        }
        HttpEntity entity;
        long startTime = System.currentTimeMillis();
        try {
            entity = new StringEntity(Json.encode(monitorData));
            httpPost.setEntity(entity);
            HttpResponse response = monitorClient.execute(httpPost);
            LOGGER.debug(response.getStatusLine().toString());
            EntityUtils.consume(response.getEntity());
        } catch (IOException e) {
            if (firestError) {
                LOGGER.error("Upload monitor data error!");
                firestError = false;
            }
        }
        long endTime = System.currentTimeMillis();
        int time = (int) (endTime - startTime);
        return time;
    }

    /**
     * 构造数据
     * @return 监控数据
     */
    MonitorData getData() {
        Collection<HystrixCommandMetrics> instances = HystrixCommandMetrics.getInstances();
        MonitorData monitorData = new MonitorData();
        monitorData.setName(RegistryUtils.getMicroservice().getServiceName());
        monitorData.setInstance(RegistryUtils.getMicroserviceInstance().getHostName());
        exactProcessInfo(monitorData);
        if (instances == null || instances.isEmpty()) {
            return monitorData;
        }
        for (HystrixCommandMetrics instance : instances) {
            monitorData.appendInterfaceInfo(instance);
        }
        return monitorData;
    }

    private void exactProcessInfo(MonitorData monitorData) {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage memoryHeapUsage = memBean.getHeapMemoryUsage();
        MemoryUsage memoryNonHeapUsage = memBean.getNonHeapMemoryUsage();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadBean.getThreadCount();
        OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();
        // this method only work well on Unix operation system
        double cpu = osMxBean.getSystemLoadAverage();
        monitorData.setCpu(cpu);
        monitorData.setThreadCount(threadCount);
        Map<String, Long> memoryInfo = new HashMap<>();
        memoryInfo.put("heapInit", memoryHeapUsage.getInit());
        memoryInfo.put("heapMax", memoryHeapUsage.getMax());
        memoryInfo.put("heapCommit", memoryHeapUsage.getCommitted());
        memoryInfo.put("heapUsed", memoryHeapUsage.getUsed());
        memoryInfo.put("nonHeapInit", memoryNonHeapUsage.getInit());
        memoryInfo.put("nonHeapCommit", memoryNonHeapUsage.getCommitted());
        memoryInfo.put("nonHeapUsed", memoryNonHeapUsage.getUsed());
        monitorData.setMemory(memoryInfo);
    }

    /**
     * 获取数据上报地址
     * @return
     */
    public String getServerUrl() {
        DynamicStringProperty property = DynamicPropertyFactory.getInstance()
                .getStringProperty("cse.monitor.client.serverUri", "https://cse-dashbord-service:30109");
        return property.getValue();
    }

    /**
     * 获取是否上报数据
     * @return
     */
    public boolean getEnableMonitor() {
        DynamicBooleanProperty property = DynamicPropertyFactory.getInstance()
                .getBooleanProperty("cse.monitor.client.enable", true);
        return property.getValue();
    }
}
