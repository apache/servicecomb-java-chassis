/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.bizkeeper;

import static com.netflix.hystrix.strategy.properties.HystrixPropertiesChainedProperty.forBoolean;
import static com.netflix.hystrix.strategy.properties.HystrixPropertiesChainedProperty.forInteger;
import static com.netflix.hystrix.strategy.properties.HystrixPropertiesChainedProperty.forString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.strategy.properties.HystrixDynamicProperty;
import com.netflix.hystrix.strategy.properties.HystrixProperty;

public class HystrixCommandPropertiesExt extends HystrixCommandProperties {

  private static final Logger LOGGER = LoggerFactory.getLogger(HystrixCommandProperties.class);

  /* defaults */
  /* package */
  // default => statisticalWindow: 10000 = 10 seconds (and default of 10
  // buckets so each bucket is 1 second)
  static final Integer DEFAULT_METRICSROLLINGSTATISTICALWINDOW = 10000;

  // default => statisticalWindowBuckets: 10 = 10 buckets in a 10 second
  // window so each bucket is 1 second
  private static final Integer DEFAULT_METRICSROLLINGSTATISTICALWINDOWBUCKETS = 10;

  // default => statisticalWindowVolumeThreshold: 20 requests in 10 seconds
  // must occur before statistics matter
  private static final Integer DEFAULT_CIRCUITBREAKERREQUESTVOLUMETHRESHOLD = 20;

  // default => sleepWindow: 15000 = 15 seconds that we will sleep before trying
  // again after tripping the circuit
  private static final Integer DEFAULT_CIRCUITBREAKERSLEEPWINDOWINMILLISECONDS = 15000;

  // default => errorThresholdPercentage = 50 = if 50%+ of requests in 10
  // seconds are failures or latent then we will trip the circuit
  private static final Integer DEFAULT_CIRCUITBREAKERERRORTHRESHOLDPERCENTAGE = 50;

  // default => forceCircuitOpen = false (we want to allow traffic)
  private static final Boolean DEFAULT_CIRCUITBREAKERFORCEOPEN = false;

  /* package */
  // default => ignoreErrors = false
  static final Boolean DEFAULT_CIRCUITBREAKERFORCECLOSED = false;

  // default => executionTimeoutInMilliseconds: 30000 = 30 second
  private static final Integer DEFAULT_EXECUTIONTIMEOUTINMILLISECONDS = 30000;

  private static final Boolean DEFAULT_EXECUTIONTIMEOUTENABLED = false;

  private static final ExecutionIsolationStrategy DEFAULT_ISOLATIONSTRATEGY = ExecutionIsolationStrategy.SEMAPHORE;

  private static final Boolean DEFAULT_EXECUTIONISOLATIONTHREADINTERRUPTONTIMEOUT = true;

  private static final Boolean DEFAULT_METRICSROLLINGPERCENTILEENABLED = false;

  private static final Boolean DEFAULT_REQUESTCACHEENABLED = true;

  private static final Integer DEFAULT_FALLBACKISOLATIONSEMAPHOREMAXCONCURRENTREQUESTS = 10;

  private static final Boolean DEFAULT_FALLBACKENABLED = true;

  private static final Integer DEFAULT_EXECUTIONISOLATIONSEMAPHOREMAXCONCURRENTREQUESTS = 1000;

  private static final Boolean DEFAULT_REQUESTLOGENABLED = true;

  private static final Boolean DEFAULT_CIRCUITBREAKERENABLED = true;

  // default to 1 minute for RollingPercentile
  private static final Integer DEFAULT_METRICSROLLINGPERCENTILEWINDOW = 60000;

  // default to 6 buckets (10 seconds each in 60 second window)
  private static final Integer DEFAULT_METRICSROLLINGPERCENTILEWINDOWBUCKETS = 6;

  // default to 100 values max per bucket
  private static final Integer DEFAULT_METRICSROLLINGPERCENTILEBUCKETSIZE = 100;

  // default to 1000ms as max frequency between allowing snapshots of health
  // (error percentage etc)
  private static final Integer DEFAULT_METRICSHEALTHSNAPSHOTINTERVALINMILLISECONDS = 1000;

  private static final int COMMAND_KEY_LENGTH = 3;

  @SuppressWarnings("unused")
  private final HystrixCommandKey key;

  // number of requests that must be made within a statisticalWindow before
  // open/close decisions are made using stats
  private final HystrixProperty<Integer> circuitBreakerRequestVolumeThreshold;

  // milliseconds after tripping circuit before allowing retry
  private final HystrixProperty<Integer> circuitBreakerSleepWindowInMilliseconds;

  // Whether circuit breaker should be enabled.
  private final HystrixProperty<Boolean> circuitBreakerEnabled;

  // % of 'marks' that must be failed to trip the circuit
  private final HystrixProperty<Integer> circuitBreakerErrorThresholdPercentage;

  // a property to allow forcing the circuit open (stopping all requests)
  private final HystrixProperty<Boolean> circuitBreakerForceOpen;

  // a property to allow ignoring errors and therefore never trip 'open' (ie.
  // allow all traffic through)
  private final HystrixProperty<Boolean> circuitBreakerForceClosed;

  // Whether a command should be executed in a separate thread or not.
  private final HystrixProperty<ExecutionIsolationStrategy> executionIsolationStrategy;

  // Timeout value in milliseconds for a command
  private final HystrixProperty<Integer> executionTimeoutInMilliseconds;

  // Whether timeout should be triggered
  private final HystrixProperty<Boolean> executionTimeoutEnabled;

  // What thread-pool this command should run in (if running on a separate
  // thread).
  private final HystrixProperty<String> executionIsolationThreadPoolKeyOverride;

  // Number of permits for execution semaphore
  private final HystrixProperty<Integer> executionIsolationSemaphoreMaxConcurrentRequests;

  // Number of permits for fallback semaphore
  private final HystrixProperty<Integer> fallbackIsolationSemaphoreMaxConcurrentRequests;

  // Whether fallback should be attempted.
  private final HystrixProperty<Boolean> fallbackEnabled;

  // Whether an underlying Future/Thread (when runInSeparateThread == true)
  // should be interrupted after a timeout
  private final HystrixProperty<Boolean> executionIsolationThreadInterruptOnTimeout;

  // milliseconds back that will be tracked
  private final HystrixProperty<Integer> metricsRollingStatisticalWindowInMilliseconds;

  // number of buckets in the statisticalWindow
  private final HystrixProperty<Integer> metricsRollingStatisticalWindowBuckets;

  // Whether monitoring should be enabled (SLA and Tracers).
  private final HystrixProperty<Boolean> metricsRollingPercentileEnabled;

  // number of milliseconds that will be tracked in RollingPercentile
  private final HystrixProperty<Integer> metricsRollingPercentileWindowInMilliseconds;

  // number of buckets percentileWindow will be divided into
  private final HystrixProperty<Integer> metricsRollingPercentileWindowBuckets;

  // how many values will be stored in each percentileWindowBucket
  private final HystrixProperty<Integer> metricsRollingPercentileBucketSize;

  // time between health snapshots
  private final HystrixProperty<Integer> metricsHealthSnapshotIntervalInMilliseconds;

  // whether command request logging is enabled.
  private final HystrixProperty<Boolean> requestLogEnabled;

  // Whether request caching is enabled.
  private final HystrixProperty<Boolean> requestCacheEnabled;

  protected HystrixCommandPropertiesExt(HystrixCommandKey key) {
    this(key, HystrixCommandProperties.Setter(), "cse");
  }

  protected HystrixCommandPropertiesExt(HystrixCommandKey key, HystrixCommandProperties.Setter builder) {
    this(key, builder, "cse");
  }

  protected HystrixCommandPropertiesExt(HystrixCommandKey key, HystrixCommandProperties.Setter builder,
      String propertyPrefix) {
    super(key, builder, propertyPrefix);
    this.key = key;
    this.circuitBreakerEnabled = getProperty(propertyPrefix,
        "circuitBreaker",
        key,
        "enabled",
        builder.getCircuitBreakerEnabled(),
        DEFAULT_CIRCUITBREAKERENABLED);
    this.circuitBreakerRequestVolumeThreshold = getProperty(propertyPrefix,
        "circuitBreaker",
        key,
        "requestVolumeThreshold",
        builder.getCircuitBreakerRequestVolumeThreshold(),
        DEFAULT_CIRCUITBREAKERREQUESTVOLUMETHRESHOLD);
    this.circuitBreakerSleepWindowInMilliseconds = getProperty(propertyPrefix,
        "circuitBreaker",
        key,
        "sleepWindowInMilliseconds",
        builder.getCircuitBreakerSleepWindowInMilliseconds(),
        DEFAULT_CIRCUITBREAKERSLEEPWINDOWINMILLISECONDS);
    this.circuitBreakerErrorThresholdPercentage = getProperty(propertyPrefix,
        "circuitBreaker",
        key,
        "errorThresholdPercentage",
        builder.getCircuitBreakerErrorThresholdPercentage(),
        DEFAULT_CIRCUITBREAKERERRORTHRESHOLDPERCENTAGE);
    this.circuitBreakerForceOpen = getProperty(propertyPrefix,
        "circuitBreaker",
        key,
        "forceOpen",
        builder.getCircuitBreakerForceOpen(),
        DEFAULT_CIRCUITBREAKERFORCEOPEN);
    this.circuitBreakerForceClosed = getProperty(propertyPrefix,
        "circuitBreaker",
        key,
        "forceClosed",
        builder.getCircuitBreakerForceClosed(),
        DEFAULT_CIRCUITBREAKERFORCECLOSED);
    this.executionIsolationStrategy = getProperty(propertyPrefix,
        "isolation",
        key,
        "strategy",
        builder.getExecutionIsolationStrategy(),
        DEFAULT_ISOLATIONSTRATEGY);
    this.executionTimeoutInMilliseconds = getProperty(propertyPrefix,
        "isolation",
        key,
        "timeoutInMilliseconds",
        builder.getExecutionTimeoutInMilliseconds(),
        DEFAULT_EXECUTIONTIMEOUTINMILLISECONDS);
    this.executionTimeoutEnabled = getProperty(propertyPrefix,
        "isolation",
        key,
        "timeout.enabled",
        builder.getExecutionTimeoutEnabled(),
        DEFAULT_EXECUTIONTIMEOUTENABLED);
    this.executionIsolationThreadInterruptOnTimeout = getProperty(propertyPrefix,
        "isolation",
        key,
        "interruptOnTimeout",
        builder.getExecutionIsolationThreadInterruptOnTimeout(),
        DEFAULT_EXECUTIONISOLATIONTHREADINTERRUPTONTIMEOUT);
    this.executionIsolationSemaphoreMaxConcurrentRequests = getProperty(propertyPrefix,
        "isolation",
        key,
        "maxConcurrentRequests",
        builder.getExecutionIsolationSemaphoreMaxConcurrentRequests(),
        DEFAULT_EXECUTIONISOLATIONSEMAPHOREMAXCONCURRENTREQUESTS);
    this.fallbackIsolationSemaphoreMaxConcurrentRequests = getProperty(propertyPrefix,
        "fallback",
        key,
        "maxConcurrentRequests",
        builder.getFallbackIsolationSemaphoreMaxConcurrentRequests(),
        DEFAULT_FALLBACKISOLATIONSEMAPHOREMAXCONCURRENTREQUESTS);
    this.fallbackEnabled = getProperty(propertyPrefix,
        "fallback",
        key,
        "enabled",
        builder.getFallbackEnabled(),
        DEFAULT_FALLBACKENABLED);
    this.metricsRollingStatisticalWindowInMilliseconds = getProperty(propertyPrefix,
        "metrics",
        key,
        "rollingStats.timeInMilliseconds",
        builder.getMetricsRollingStatisticalWindowInMilliseconds(),
        DEFAULT_METRICSROLLINGSTATISTICALWINDOW);
    this.metricsRollingStatisticalWindowBuckets = getProperty(propertyPrefix,
        "metrics",
        key,
        "rollingStats.numBuckets",
        builder.getMetricsRollingStatisticalWindowBuckets(),
        DEFAULT_METRICSROLLINGSTATISTICALWINDOWBUCKETS);
    this.metricsRollingPercentileEnabled = getProperty(propertyPrefix,
        "metrics",
        key,
        "rollingPercentile.enabled",
        builder.getMetricsRollingPercentileEnabled(),
        DEFAULT_METRICSROLLINGPERCENTILEENABLED);
    this.metricsRollingPercentileWindowInMilliseconds = getProperty(propertyPrefix,
        "metrics",
        key,
        "rollingPercentile.timeInMilliseconds",
        builder.getMetricsRollingPercentileWindowInMilliseconds(),
        DEFAULT_METRICSROLLINGPERCENTILEWINDOW);
    this.metricsRollingPercentileWindowBuckets = getProperty(propertyPrefix,
        "metrics",
        key,
        "rollingPercentile.numBuckets",
        builder.getMetricsRollingPercentileWindowBuckets(),
        DEFAULT_METRICSROLLINGPERCENTILEWINDOWBUCKETS);
    this.metricsRollingPercentileBucketSize = getProperty(propertyPrefix,
        "metrics",
        key,
        "rollingPercentile.bucketSize",
        builder.getMetricsRollingPercentileBucketSize(),
        DEFAULT_METRICSROLLINGPERCENTILEBUCKETSIZE);
    this.metricsHealthSnapshotIntervalInMilliseconds = getProperty(propertyPrefix,
        "metrics",
        key,
        "healthSnapshot.intervalInMilliseconds",
        builder.getMetricsHealthSnapshotIntervalInMilliseconds(),
        DEFAULT_METRICSHEALTHSNAPSHOTINTERVALINMILLISECONDS);
    this.requestCacheEnabled = getProperty(propertyPrefix,
        "requestCache",
        key,
        "enabled",
        builder.getRequestCacheEnabled(),
        DEFAULT_REQUESTCACHEENABLED);
    this.requestLogEnabled = getProperty(propertyPrefix,
        "requestLog",
        key,
        "enabled",
        builder.getRequestLogEnabled(),
        DEFAULT_REQUESTLOGENABLED);

    // threadpool doesn't have a global override, only instance level makes
    // sense
    this.executionIsolationThreadPoolKeyOverride = forString()
        .add(propertyPrefix + ".command." + key.name() + ".threadPoolKeyOverride", null)
        .build();
  }

  @Override
  public HystrixProperty<Boolean> circuitBreakerEnabled() {
    return circuitBreakerEnabled;
  }

  @Override
  public HystrixProperty<Integer> circuitBreakerErrorThresholdPercentage() {
    return circuitBreakerErrorThresholdPercentage;
  }

  @Override
  public HystrixProperty<Boolean> circuitBreakerForceClosed() {
    return circuitBreakerForceClosed;
  }

  @Override
  public HystrixProperty<Boolean> circuitBreakerForceOpen() {
    return circuitBreakerForceOpen;
  }

  @Override
  public HystrixProperty<Integer> circuitBreakerRequestVolumeThreshold() {
    return circuitBreakerRequestVolumeThreshold;
  }

  @Override
  public HystrixProperty<Integer> circuitBreakerSleepWindowInMilliseconds() {
    return circuitBreakerSleepWindowInMilliseconds;
  }

  @Override
  public HystrixProperty<Integer> executionIsolationSemaphoreMaxConcurrentRequests() {
    return executionIsolationSemaphoreMaxConcurrentRequests;
  }

  @Override
  public HystrixProperty<ExecutionIsolationStrategy> executionIsolationStrategy() {
    return executionIsolationStrategy;
  }

  @Override
  public HystrixProperty<Boolean> executionIsolationThreadInterruptOnTimeout() {
    return executionIsolationThreadInterruptOnTimeout;
  }

  @Override
  public HystrixProperty<String> executionIsolationThreadPoolKeyOverride() {
    return executionIsolationThreadPoolKeyOverride;
  }

  @Override
  @Deprecated // prefer {@link #executionTimeoutInMilliseconds}
  public HystrixProperty<Integer> executionIsolationThreadTimeoutInMilliseconds() {
    return executionTimeoutInMilliseconds;
  }

  @Override
  public HystrixProperty<Integer> executionTimeoutInMilliseconds() {
    return executionIsolationThreadTimeoutInMilliseconds();
  }

  @Override
  public HystrixProperty<Boolean> executionTimeoutEnabled() {
    return executionTimeoutEnabled;
  }

  @Override
  public HystrixProperty<Integer> fallbackIsolationSemaphoreMaxConcurrentRequests() {
    return fallbackIsolationSemaphoreMaxConcurrentRequests;
  }

  @Override
  public HystrixProperty<Boolean> fallbackEnabled() {
    return fallbackEnabled;
  }

  @Override
  public HystrixProperty<Integer> metricsHealthSnapshotIntervalInMilliseconds() {
    return metricsHealthSnapshotIntervalInMilliseconds;
  }

  @Override
  public HystrixProperty<Integer> metricsRollingPercentileBucketSize() {
    return metricsRollingPercentileBucketSize;
  }

  @Override
  public HystrixProperty<Boolean> metricsRollingPercentileEnabled() {
    return metricsRollingPercentileEnabled;
  }

  @Override
  public HystrixProperty<Integer> metricsRollingPercentileWindow() {
    return metricsRollingPercentileWindowInMilliseconds;
  }

  @Override
  public HystrixProperty<Integer> metricsRollingPercentileWindowInMilliseconds() {
    return metricsRollingPercentileWindowInMilliseconds;
  }

  @Override
  public HystrixProperty<Integer> metricsRollingPercentileWindowBuckets() {
    return metricsRollingPercentileWindowBuckets;
  }

  @Override
  public HystrixProperty<Integer> metricsRollingStatisticalWindowInMilliseconds() {
    return metricsRollingStatisticalWindowInMilliseconds;
  }

  @Override
  public HystrixProperty<Integer> metricsRollingStatisticalWindowBuckets() {
    return metricsRollingStatisticalWindowBuckets;
  }

  @Override
  public HystrixProperty<Boolean> requestCacheEnabled() {
    return requestCacheEnabled;
  }

  @Override
  public HystrixProperty<Boolean> requestLogEnabled() {
    return requestLogEnabled;
  }

  private HystrixProperty<ExecutionIsolationStrategy> getProperty(String propertyPrefix, String command,
      HystrixCommandKey key, String instanceProperty, ExecutionIsolationStrategy builderOverrideValue,
      ExecutionIsolationStrategy defaultValue) {
    return new ExecutionIsolationStrategyHystrixProperty(builderOverrideValue, key, propertyPrefix, command,
        defaultValue, instanceProperty);
  }

  private static final class ExecutionIsolationStrategyHystrixProperty
      implements HystrixProperty<ExecutionIsolationStrategy> {
    private final HystrixDynamicProperty<String> property;

    private volatile ExecutionIsolationStrategy value;

    private final ExecutionIsolationStrategy defaultValue;

    private ExecutionIsolationStrategyHystrixProperty(ExecutionIsolationStrategy builderOverrideValue,
        HystrixCommandKey key, String propertyPrefix, String command, ExecutionIsolationStrategy defaultValue,
        String instanceProperty) {
      this.defaultValue = defaultValue;
      String overrideValue = null;
      if (builderOverrideValue != null) {
        overrideValue = builderOverrideValue.name();
      }
      property = forString()
          .add(propertyPrefix + "." + command + "." + key.name() + "." + instanceProperty, null)
          .add(propertyPrefix + "." + command + "." + serviceKey(key.name()) + "." + instanceProperty,
              overrideValue)
          .add(propertyPrefix + "." + command + "." + typeKey(key.name()) + "." + instanceProperty,
              defaultValue.name())
          .build();

      // initialize the enum value from the property
      parseProperty();

      // use a callback to handle changes so we only handle the parse cost
      // on updates rather than every fetch
      // when the property value changes we'll update the value
      property.addCallback(this::parseProperty);
    }

    @Override
    public ExecutionIsolationStrategy get() {
      return value;
    }

    private void parseProperty() {
      try {
        value = ExecutionIsolationStrategy.valueOf(property.get());
      } catch (Exception e) {
        LOGGER.error("Unable to derive ExecutionIsolationStrategy from property value: " + property.get(), e);
        // use the default value
        value = defaultValue;
      }
    }
  }

  private static String serviceKey(String key) {
    String[] keyparts = key.split("\\.", COMMAND_KEY_LENGTH);
    if (keyparts.length == COMMAND_KEY_LENGTH) {
      return keyparts[0] + "." + keyparts[1];
    }
    return key;
  }

  private static String typeKey(String key) {
    int index = key.indexOf(".");
    if (index > 0) {
      return key.substring(0, index);
    }
    return key;
  }

  private HystrixProperty<Integer> getProperty(String propertyPrefix, String command, HystrixCommandKey key,
      String instanceProperty, Integer builderOverrideValue,
      Integer defaultValue) {
    return forInteger()
        .add(propertyPrefix + "." + command + "." + key.name() + "." + instanceProperty, null)
        .add(propertyPrefix + "." + command + "." + serviceKey(key.name()) + "." + instanceProperty,
            null)
        .add(propertyPrefix + "." + command + "." + typeKey(key.name()) + "." + instanceProperty,
            builderOverrideValue == null ? defaultValue : builderOverrideValue)
        .build();
  }

  private HystrixProperty<Boolean> getProperty(String propertyPrefix, String command, HystrixCommandKey key,
      String instanceProperty, Boolean builderOverrideValue, Boolean defaultValue) {
    return forBoolean()
        .add(propertyPrefix + "." + command + "." + key.name() + "." + instanceProperty, null)
        .add(propertyPrefix + "." + command + "." + serviceKey(key.name()) + "." + instanceProperty,
            null)
        .add(propertyPrefix + "." + command + "." + typeKey(key.name()) + "." + instanceProperty,
            builderOverrideValue == null ? defaultValue : builderOverrideValue)
        .build();
  }
}
