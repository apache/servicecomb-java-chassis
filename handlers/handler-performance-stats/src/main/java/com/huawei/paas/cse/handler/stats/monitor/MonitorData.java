package com.huawei.paas.cse.handler.stats.monitor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixEventType;

/**
 * 监控上报的数据，每次上报一条数据
 * @author w00293972
 */
public class MonitorData {
    private static final double PERCENTAGE_995 = 99.5;
    private static final double PERCENTAGE_99 = 99;
    private static final double PERCENTAGE_90 = 90;
    private static final double PERCENTAGE_75 = 75;
    private static final double PERCENTAGE_50 = 50;
    private static final double PERCENTAGE_25 = 25;
    private static final double PERCENTAGE_5 = 5;
    private static final int SCALE_VAL = 1;
    /**
     * 服务名称
     */
    private String name;
    /**
     * 服务实例名称
     */
    private String instance;

    private int thread;
    private double cpu;
    private Map<String, Long> memory;
    
    /**
     * 接口相关监控数据
     */
    private List<InterfaceInfo> interfaces = new ArrayList<>();

    /**
     * 用户自定义的一些变量
     */
    private Map<String, Object> customs;

    public void appendInterfaceInfo(HystrixCommandMetrics metrics) {
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        int windowTime = metrics.getProperties().metricsRollingStatisticalWindowInMilliseconds().get() / 1000;
        HystrixEventType[] events = {HystrixEventType.SUCCESS, HystrixEventType.FAILURE};
        long rollingTotal = 0;
        long cumulativeTotal = 0;

        for (HystrixEventType event : events) {
            long val = metrics.getRollingCount(event);
            long cumVal = metrics.getCumulativeCount(event);
            rollingTotal += val;
            cumulativeTotal += cumVal;
        }
        long successCount = metrics.getRollingCount(HystrixEventType.SUCCESS);
        long failureCount = metrics.getRollingCount(HystrixEventType.FAILURE);
        long semRejectCount = metrics.getRollingCount(HystrixEventType.SEMAPHORE_REJECTED);
        long threadRejectCount = metrics.getRollingCount(HystrixEventType.THREAD_POOL_REJECTED);
        long timeoutCount = metrics.getRollingCount(HystrixEventType.TIMEOUT);

        int latency = metrics.getExecutionTimeMean();
        int latency995 = metrics.getExecutionTimePercentile(PERCENTAGE_995);
        int latency99 = metrics.getExecutionTimePercentile(PERCENTAGE_99);
        int latency90 = metrics.getExecutionTimePercentile(PERCENTAGE_90);
        int latency75 = metrics.getExecutionTimePercentile(PERCENTAGE_75);
        int latency50 = metrics.getExecutionTimePercentile(PERCENTAGE_50);
        int latency25 = metrics.getExecutionTimePercentile(PERCENTAGE_25);
        int latency5 = metrics.getExecutionTimePercentile(PERCENTAGE_5);

        interfaceInfo.setName(metrics.getCommandKey().name());
        interfaceInfo.setCircuitBreakerOpen(metrics.getProperties().circuitBreakerEnabled().get());
        interfaceInfo.setFailure(failureCount);
        interfaceInfo.setSemaphoreRejected(semRejectCount);
        interfaceInfo.setThreadPoolRejected(threadRejectCount);
        interfaceInfo.setCountTimeout(timeoutCount);
        interfaceInfo.setDesc(metrics.getCommandKey().name());
        interfaceInfo.setLatency(latency);
        interfaceInfo.setL25(latency25);
        interfaceInfo.setL5(latency5);
        interfaceInfo.setL50(latency50);
        interfaceInfo.setL75(latency75);
        interfaceInfo.setL90(latency90);
        interfaceInfo.setL99(latency99);
        interfaceInfo.setL995(latency995);
        interfaceInfo.setTotal(cumulativeTotal);
        double qpsVal = ((double) rollingTotal) / windowTime;
        BigDecimal b = new BigDecimal(qpsVal);
        BigDecimal qps = b.setScale(SCALE_VAL, RoundingMode.HALF_DOWN);
        interfaceInfo.setQps(qps.doubleValue());
        if (rollingTotal == 0) {
            interfaceInfo.setRate(100);
        } else {
            interfaceInfo.setRate(((double) successCount) / rollingTotal);
        }
        interfaces.add(interfaceInfo);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public List<InterfaceInfo> getInterfaces() {
        return interfaces;
    }

    public Map<String, Object> getCustoms() {
        return customs;
    }

    public void setCustoms(Map<String, Object> customs) {
        this.customs = customs;
    }

    public double getCpu() {
        return cpu;
    }

    public void setCpu(double cpu) {
        this.cpu = cpu;
    }

    public int getThreadCount() {
        return thread;
    }

    public void setThreadCount(int threadCount) {
        this.thread = threadCount;
    }

    public Map<String, Long> getMemory() {
        return memory;
    }

    public void setMemory(Map<String, Long> memory) {
        this.memory = memory;
    }

    /**
     * 接口相关监控数据
     * @author w00293972
     */
    public class InterfaceInfo {
        /**
         * 接口名称
         */
        private String name;
        /**
         * 接口描述
         */
        private String desc;
        /**
         * 每秒请求量，单位个
         */
        private double qps;
        /**
         * 时延，单位毫秒
         */
        private int latency;

        private int l995;

        private int l99;

        private int l90;

        private int l75;

        private int l50;

        private int l25;

        private int l5;
        /**
         * 成功率，百分比
         */
        private double rate;
        /**
         * 请求总量
         */
        private long total;
        /**
         * 当前熔断状态
         */
        private boolean isCircuitBreakerOpen;
        /**
         * 失败总个数
         */
        private long failure;
        /**
         * 总短路个数
         */
        private long shortCircuited;
        /**
         * 总信号量拒绝个数
         */
        private long semaphoreRejected;
        /**
         * 总线程池拒绝个数
         */
        private long threadPoolRejected;
        /**
         * 总超时个数
         */
        private long countTimeout;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public double getQps() {
            return qps;
        }

        public void setQps(double qps) {
            this.qps = qps;
        }

        public int getLatency() {
            return latency;
        }

        public void setLatency(int latency) {
            this.latency = latency;
        }

        public int getL995() {
            return l995;
        }

        public void setL995(int l995) {
            this.l995 = l995;
        }

        public int getL99() {
            return l99;
        }

        public void setL99(int l99) {
            this.l99 = l99;
        }

        public int getL90() {
            return l90;
        }

        public void setL90(int l90) {
            this.l90 = l90;
        }

        public int getL75() {
            return l75;
        }

        public void setL75(int l75) {
            this.l75 = l75;
        }

        public int getL50() {
            return l50;
        }

        public void setL50(int l50) {
            this.l50 = l50;
        }

        public int getL25() {
            return l25;
        }

        public void setL25(int l25) {
            this.l25 = l25;
        }

        public int getL5() {
            return l5;
        }

        public void setL5(int l5) {
            this.l5 = l5;
        }

        public double getRate() {
            return rate;
        }

        public void setRate(double rate) {
            this.rate = rate;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public boolean isCircuitBreakerOpen() {
            return isCircuitBreakerOpen;
        }

        public void setCircuitBreakerOpen(boolean isCircuitBreakerOpen) {
            this.isCircuitBreakerOpen = isCircuitBreakerOpen;
        }

        public long getFailure() {
            return failure;
        }

        public void setFailure(long failure) {
            this.failure = failure;
        }

        public long getShortCircuited() {
            return shortCircuited;
        }

        public void setShortCircuited(long shortCircuited) {
            this.shortCircuited = shortCircuited;
        }

        public long getSemaphoreRejected() {
            return semaphoreRejected;
        }

        public void setSemaphoreRejected(long semaphoreRejected) {
            this.semaphoreRejected = semaphoreRejected;
        }

        public long getThreadPoolRejected() {
            return threadPoolRejected;
        }

        public void setThreadPoolRejected(long threadPoolRejected) {
            this.threadPoolRejected = threadPoolRejected;
        }

        public long getCountTimeout() {
            return countTimeout;
        }

        public void setCountTimeout(long countTimeout) {
            this.countTimeout = countTimeout;
        }

    }
}
