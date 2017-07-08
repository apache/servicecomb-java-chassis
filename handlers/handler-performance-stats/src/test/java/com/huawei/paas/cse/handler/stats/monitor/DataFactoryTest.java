package com.huawei.paas.cse.handler.stats.monitor;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Test;

import com.huawei.paas.cse.handler.stats.monitor.MonitorData.InterfaceInfo;
import com.huawei.paas.foundation.auth.HttpClientFactory;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixEventType;
import com.netflix.hystrix.strategy.properties.HystrixProperty;

import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

@SuppressWarnings("deprecation")
public class DataFactoryTest {

    @Test
    public void testStart() {
        Constructor<?>[] cs = HttpClientFactory.class.getDeclaredConstructors();
        for (Constructor<?> c : cs) {
            c.setAccessible(true);
            try {
                c.newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        
        new MockUp<RegistryUtils> () {
            @Mock
            public Microservice getMicroservice() {
                return new Microservice();
            }
        };
        
        DataFactory.getInstance().start();
        MonitorData data = DataFactory.getInstance().getData();
        Assert.assertNotNull(DataFactory.getInstance().getServerUrl());
        Assert.assertNotNull(data);
    }

    @Test
    public void testSendData(@Mocked HystrixCommandMetrics metric, @Mocked HttpClient httpClient, @Mocked CloseableHttpResponse resp, @Mocked HystrixCommandProperties properties,
            @Mocked HystrixProperty<Integer> mill) throws IOException {

        HttpClient client = new CloseableHttpClient() {
            int times = 0;
            @Override
            public CloseableHttpResponse execute(HttpUriRequest request)
                    throws IOException, ClientProtocolException {
                if (times == 0) {
                    times ++;
                    return resp;
                }
                else {
                    throw new IOException();
                }
            }

            @Override
            public void close() throws IOException {
            }

            @Override
            public HttpParams getParams() {
                return null;
            }

            @Override
            public ClientConnectionManager getConnectionManager() {
                return null;
            }

            @Override
            protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context)
                    throws IOException, ClientProtocolException {
                return null;
            }
        };
        new MockUp<HttpClientFactory>() {
            @Mock
            public HttpClient getOrCreateHttpClient(String key) {
                return client;
            }
        };
        Collection<HystrixCommandMetrics> metrics = new ArrayList<HystrixCommandMetrics>();
        metrics.add(metric);
        new MockUp<HystrixCommandMetrics> () {
            @Mock
            public Collection<HystrixCommandMetrics> getInstances() {
                return null;
            }
        };
        //第一次模拟异常情况发布
        DataFactory.getInstance().sendData(new MonitorData());
        //第二次模拟正常情况
        int time = DataFactory.getInstance().sendData(new MonitorData());
        Assert.assertTrue(time >= 0);
    }

    @Test
    public void testMonitorData(@Mocked HystrixCommandMetrics metrics, @Mocked HystrixCommandProperties properties,
            @Mocked HystrixProperty<Integer> mill, @Mocked HystrixCommandMetrics metricssecond) {
        HystrixProperty<Boolean> circuit = new HystrixProperty<Boolean>() {
            @Override
            public Boolean get() {
                return true;
            }
        };
        new Expectations() {
            {
                metrics.getProperties();
                result = properties;
                properties.metricsRollingStatisticalWindowInMilliseconds();
                result = mill;
                mill.get();
                result = 10000;
                properties.circuitBreakerEnabled();
                result = circuit;
                metrics.getCumulativeCount(HystrixEventType.SUCCESS);
                result = 110;
                metrics.getRollingCount(HystrixEventType.SUCCESS);
                result = 11;

                metricssecond.getProperties();
                result = properties;
                properties.metricsRollingStatisticalWindowInMilliseconds();
                result = mill;
                mill.get();
                result = 10000;
                properties.circuitBreakerEnabled();
                result = circuit;
                metricssecond.getCumulativeCount(HystrixEventType.SUCCESS);
                result = 110;
                metricssecond.getRollingCount(HystrixEventType.SUCCESS);
                result = 0;
            }
        };
        MonitorData data = new MonitorData();
        data.setName("test");
        data.setInstance("hello");
        data.setCustoms(new HashMap<>());
        data.appendInterfaceInfo(metrics);
        data.appendInterfaceInfo(metricssecond);
        data.getCustoms();
        data.getInstance();
        InterfaceInfo info = data.getInterfaces().get(0);
        info.setShortCircuited(1);
        Method[] methods = info.getClass().getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().startsWith("get") || m.getName().startsWith("is")) {
                try {
                    m.invoke(info);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Assert.assertEquals("test", data.getName());
        Assert.assertEquals(2, data.getInterfaces().size());
    }

}
