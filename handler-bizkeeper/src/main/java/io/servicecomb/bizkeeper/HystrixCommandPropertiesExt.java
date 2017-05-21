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

package io.servicecomb.bizkeeper;

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
    // default => sleepWindow: 5000 = 5 seconds that we will sleep before trying
    // again after tripping the circuit
    private static final Integer DEFAULT_CIRCUITBREAKERSLEEPWINDOWINMILLISECONDS = 5000;
    // default => errorThresholdPercentage = 50 = if 50%+ of requests in 10
    // seconds are failures or latent then we will trip the circuit
    private static final Integer DEFAULT_CIRCUITBREAKERERRORTHRESHOLDPERCENTAGE = 50;
    // default => forceCircuitOpen = false (we want to allow traffic)
    private static final Boolean DEFAULT_CIRCUITBREAKERFORCEOPEN = false;
    /* package */
    // default => ignoreErrors = false
    static final Boolean DEFAULT_CIRCUITBREAKERFORCECLOSED = false;
    // default => executionTimeoutInMilliseconds: 1000 = 1 second
    private static final Integer DEFAULT_EXECUTIONTIMEOUTINMILLISECONDS = 1000;
    private static final Boolean DEFAULT_EXECUTIONTIMEOUTENABLED = true;
    private static final ExecutionIsolationStrategy DEFAULT_ISOLATIONSTRATEGY = ExecutionIsolationStrategy.THREAD;
    private static final Boolean DEFAULT_EXECUTIONISOLATIONTHREADINTERRUPTONTIMEOUT = true;
    private static final Boolean DEFAULT_METRICSROLLINGPERCENTILEENABLED = true;
    private static final Boolean DEFAULT_REQUESTCACHEENABLED = true;
    private static final Integer DEFAULT_FALLBACKISOLATIONSEMAPHOREMAXCONCURRENTREQUESTS = 10;
    private static final Boolean DEFAULT_FALLBACKENABLED = true;
    private static final Integer DEFAULT_EXECUTIONISOLATIONSEMAPHOREMAXCONCURRENTREQUESTS = 10;
    private static final Boolean DEFAULT_REQUESTLOGENABLED = true;
    private static final Boolean DEFAULT_CIRCUITBREAKERENABLED = true;
    // default to 1 minute for RollingPercentile
    private static final Integer DEFAULT_METRICSROLLINGPERCENTILEWINDOW = 60000;
    // default to 6 buckets (10 seconds each in 60 second window)
    private static final Integer DEFAULT_METRICSROLLINGPERCENTILEWINDOWBUCKETS = 6;
    // default to 100 values max per bucket
    private static final Integer DEFAULT_METRICSROLLINGPERCENTILEBUCKETSIZE = 100;
    // default to 500ms as max frequency between allowing snapshots of health
    // (error percentage etc)
    private static final Integer DEFAULT_METRICSHEALTHSNAPSHOTINTERVALINMILLISECONDS = 500;

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
        this.circuitBreakerEnabled = getProperty(propertyPrefix, "circuitBreaker", key, "enabled",
                builder.getCircuitBreakerEnabled(), DEFAULT_CIRCUITBREAKERENABLED);
        this.circuitBreakerRequestVolumeThreshold = getProperty(propertyPrefix, "circuitBreaker", key,
                "requestVolumeThreshold", builder.getCircuitBreakerRequestVolumeThreshold(),
                DEFAULT_CIRCUITBREAKERREQUESTVOLUMETHRESHOLD);
        this.circuitBreakerSleepWindowInMilliseconds = getProperty(propertyPrefix, "circuitBreaker", key,
                "sleepWindowInMilliseconds", builder.getCircuitBreakerSleepWindowInMilliseconds(),
                DEFAULT_CIRCUITBREAKERSLEEPWINDOWINMILLISECONDS);
        this.circuitBreakerErrorThresholdPercentage = getProperty(propertyPrefix, "circuitBreaker", key,
                "errorThresholdPercentage", builder.getCircuitBreakerErrorThresholdPercentage(),
                DEFAULT_CIRCUITBREAKERERRORTHRESHOLDPERCENTAGE);
        this.circuitBreakerForceOpen = getProperty(propertyPrefix, "circuitBreaker", key, "forceOpen",
                builder.getCircuitBreakerForceOpen(), DEFAULT_CIRCUITBREAKERFORCEOPEN);
        this.circuitBreakerForceClosed = getProperty(propertyPrefix, "circuitBreaker", key, "forceClosed",
                builder.getCircuitBreakerForceClosed(), DEFAULT_CIRCUITBREAKERFORCECLOSED);
        this.executionIsolationStrategy = getProperty(propertyPrefix, "isolation", key, "strategy",
                builder.getExecutionIsolationStrategy(), DEFAULT_ISOLATIONSTRATEGY);
        this.executionTimeoutInMilliseconds = getProperty(propertyPrefix, "isolation", key, "timeoutInMilliseconds",
                builder.getExecutionTimeoutInMilliseconds(), DEFAULT_EXECUTIONTIMEOUTINMILLISECONDS);
        this.executionTimeoutEnabled = getProperty(propertyPrefix, "isolation", key, "timeout.enabled",
                builder.getExecutionTimeoutEnabled(), DEFAULT_EXECUTIONTIMEOUTENABLED);
        this.executionIsolationThreadInterruptOnTimeout = getProperty(propertyPrefix, "isolation", key,
                "interruptOnTimeout", builder.getExecutionIsolationThreadInterruptOnTimeout(),
                DEFAULT_EXECUTIONISOLATIONTHREADINTERRUPTONTIMEOUT);
        this.executionIsolationSemaphoreMaxConcurrentRequests = getProperty(propertyPrefix, "isolation", key,
                "maxConcurrentRequests", builder.getExecutionIsolationSemaphoreMaxConcurrentRequests(),
                DEFAULT_EXECUTIONISOLATIONSEMAPHOREMAXCONCURRENTREQUESTS);
        this.fallbackIsolationSemaphoreMaxConcurrentRequests = getProperty(propertyPrefix, "fallback", key,
                "maxConcurrentRequests", builder.getFallbackIsolationSemaphoreMaxConcurrentRequests(),
                DEFAULT_FALLBACKISOLATIONSEMAPHOREMAXCONCURRENTREQUESTS);
        this.fallbackEnabled = getProperty(propertyPrefix, "fallback", key, "enabled", builder.getFallbackEnabled(),
                DEFAULT_FALLBACKENABLED);
        this.metricsRollingStatisticalWindowInMilliseconds = getProperty(propertyPrefix, "metrics", key,
                "rollingStats.timeInMilliseconds", builder.getMetricsRollingStatisticalWindowInMilliseconds(),
                DEFAULT_METRICSROLLINGSTATISTICALWINDOW);
        this.metricsRollingStatisticalWindowBuckets = getProperty(propertyPrefix, "metrics", key,
                "rollingStats.numBuckets", builder.getMetricsRollingStatisticalWindowBuckets(),
                DEFAULT_METRICSROLLINGSTATISTICALWINDOWBUCKETS);
        this.metricsRollingPercentileEnabled = getProperty(propertyPrefix, "metrics", key, "rollingPercentile.enabled",
                builder.getMetricsRollingPercentileEnabled(), DEFAULT_METRICSROLLINGPERCENTILEENABLED);
        this.metricsRollingPercentileWindowInMilliseconds = getProperty(propertyPrefix, "metrics", key,
                "rollingPercentile.timeInMilliseconds", builder.getMetricsRollingPercentileWindowInMilliseconds(),
                DEFAULT_METRICSROLLINGPERCENTILEWINDOW);
        this.metricsRollingPercentileWindowBuckets = getProperty(propertyPrefix, "metrics", key,
                "rollingPercentile.numBuckets", builder.getMetricsRollingPercentileWindowBuckets(),
                DEFAULT_METRICSROLLINGPERCENTILEWINDOWBUCKETS);
        this.metricsRollingPercentileBucketSize = getProperty(propertyPrefix, "metrics", key,
                "rollingPercentile.bucketSize", builder.getMetricsRollingPercentileBucketSize(),
                DEFAULT_METRICSROLLINGPERCENTILEBUCKETSIZE);
        this.metricsHealthSnapshotIntervalInMilliseconds = getProperty(propertyPrefix, "metrics", key,
                "healthSnapshot.intervalInMilliseconds", builder.getMetricsHealthSnapshotIntervalInMilliseconds(),
                DEFAULT_METRICSHEALTHSNAPSHOTINTERVALINMILLISECONDS);
        this.requestCacheEnabled = getProperty(propertyPrefix, "requestCache", key, "enabled",
                builder.getRequestCacheEnabled(), DEFAULT_REQUESTCACHEENABLED);
        this.requestLogEnabled = getProperty(propertyPrefix, "requestLog", key, "enabled",
                builder.getRequestLogEnabled(), DEFAULT_REQUESTLOGENABLED);

        // threadpool doesn't have a global override, only instance level makes
        // sense
        this.executionIsolationThreadPoolKeyOverride = forString()
                .add(propertyPrefix + ".command." + key.name() + ".threadPoolKeyOverride", null).build();
    }

    /**
     * Whether to use a {@link HystrixCircuitBreaker} or not. If false no
     * circuit-breaker logic will be used and all requests permitted.
     * <p>
     * This is similar in effect to {@link #circuitBreakerForceClosed()} except
     * that continues tracking metrics and knowing whether it should be
     * open/closed, this property results in not even instantiating a
     * circuit-breaker.
     * @return {@code HystrixProperty<Boolean>}
     */
    public HystrixProperty<Boolean> circuitBreakerEnabled() {
        return circuitBreakerEnabled;
    }

    /**
     * Error percentage threshold (as whole number such as 50) at which point
     * the circuit breaker will trip open and reject requests.
     * <p>
     * It will stay tripped for the duration defined in
     * {@link #circuitBreakerSleepWindowInMilliseconds()};
     * <p>
     * The error percentage this is compared against comes from
     * {@link HystrixCommandMetrics#getHealthCounts()}.
     * @return {@code HystrixProperty<Integer>}
     */
    public HystrixProperty<Integer> circuitBreakerErrorThresholdPercentage() {
        return circuitBreakerErrorThresholdPercentage;
    }

    /**
     * If true the {@link HystrixCircuitBreaker#allowRequest()} will always
     * return true to allow requests regardless of the error percentage from
     * {@link HystrixCommandMetrics#getHealthCounts()}.
     * <p>
     * The {@link #circuitBreakerForceOpen()} property takes precedence so if it
     * set to true this property does nothing.
     * @return {@code HystrixProperty<Boolean>}
     */
    public HystrixProperty<Boolean> circuitBreakerForceClosed() {
        return circuitBreakerForceClosed;
    }

    /**
     * If true the {@link HystrixCircuitBreaker#allowRequest()} will always
     * return false, causing the circuit to be open (tripped) and reject all
     * requests.
     * <p>
     * This property takes precedence over {@link #circuitBreakerForceClosed()};
     * @return {@code HystrixProperty<Boolean>}
     */
    public HystrixProperty<Boolean> circuitBreakerForceOpen() {
        return circuitBreakerForceOpen;
    }

    /**
     * Minimum number of requests in the
     * {@link #metricsRollingStatisticalWindowInMilliseconds()} that must exist
     * before the {@link HystrixCircuitBreaker} will trip.
     * <p>
     * If below this number the circuit will not trip regardless of error
     * percentage.
     * @return {@code HystrixProperty<Integer>}
     */
    public HystrixProperty<Integer> circuitBreakerRequestVolumeThreshold() {
        return circuitBreakerRequestVolumeThreshold;
    }

    /**
     * The time in milliseconds after a {@link HystrixCircuitBreaker} trips open
     * that it should wait before trying requests again.
     * @return {@code HystrixProperty<Integer>}
     */
    public HystrixProperty<Integer> circuitBreakerSleepWindowInMilliseconds() {
        return circuitBreakerSleepWindowInMilliseconds;
    }

    /**
     * Number of concurrent requests permitted to {@link HystrixCommand#run()}.
     * Requests beyond the concurrent limit will be rejected.
     * <p>
     * Applicable only when {@link #executionIsolationStrategy()} == SEMAPHORE.
     * @return {@code HystrixProperty<Integer>}
     */
    public HystrixProperty<Integer> executionIsolationSemaphoreMaxConcurrentRequests() {
        return executionIsolationSemaphoreMaxConcurrentRequests;
    }

    /**
     * What isolation strategy {@link HystrixCommand#run()} will be executed
     * with.
     * <p>
     * If {@link ExecutionIsolationStrategy#THREAD} then it will be executed on
     * a separate thread and concurrent requests limited by the number of
     * threads in the thread-pool.
     * <p>
     * If {@link ExecutionIsolationStrategy#SEMAPHORE} then it will be executed
     * on the calling thread and concurrent requests limited by the semaphore
     * count.
     * @return {@code HystrixProperty<Boolean>}
     */
    public HystrixProperty<ExecutionIsolationStrategy> executionIsolationStrategy() {
        return executionIsolationStrategy;
    }

    /**
     * Whether the execution thread should attempt an interrupt (using
     * {@link Future#cancel}) when a thread times out.
     * <p>
     * Applicable only when {@link #executionIsolationStrategy()} == THREAD.
     * @return {@code HystrixProperty<Boolean>}
     */
    public HystrixProperty<Boolean> executionIsolationThreadInterruptOnTimeout() {
        return executionIsolationThreadInterruptOnTimeout;
    }

    /**
     * Allow a dynamic override of the {@link HystrixThreadPoolKey} that will
     * dynamically change which {@link HystrixThreadPool} a
     * {@link HystrixCommand} executes on.
     * <p>
     * Typically this should return NULL which will cause it to use the
     * {@link HystrixThreadPoolKey} injected into a {@link HystrixCommand} or
     * derived from the {@link HystrixCommandGroupKey}.
     * <p>
     * When set the injected or derived values will be ignored and a new
     * {@link HystrixThreadPool} created (if necessary) and the
     * {@link HystrixCommand} will begin using the newly defined pool.
     * @return {@code HystrixProperty<String>}
     */
    public HystrixProperty<String> executionIsolationThreadPoolKeyOverride() {
        return executionIsolationThreadPoolKeyOverride;
    }

    /**
     * @deprecated As of release 1.4.0, replaced by
     *             {@link #executionTimeoutInMilliseconds()}. Timeout is no
     *             longer specific to thread-isolation commands, so the
     *             thread-specific name is misleading. Time in milliseconds at
     *             which point the command will timeout and halt execution.
     *             <p>
     *             If {@link #executionIsolationThreadInterruptOnTimeout} ==
     *             true and the command is thread-isolated, the executing thread
     *             will be interrupted. If the command is semaphore-isolated and
     *             a {@link HystrixObservableCommand}, that command will get
     *             unsubscribed.
     *             <p>
     * @return {@code HystrixProperty<Integer>}
     */
    @Deprecated // prefer {@link #executionTimeoutInMilliseconds}
    public HystrixProperty<Integer> executionIsolationThreadTimeoutInMilliseconds() {
        return executionTimeoutInMilliseconds;
    }

    /**
     * Time in milliseconds at which point the command will timeout and halt
     * execution.
     * <p>
     * If {@link #executionIsolationThreadInterruptOnTimeout} == true and the
     * command is thread-isolated, the executing thread will be interrupted. If
     * the command is semaphore-isolated and a {@link HystrixObservableCommand},
     * that command will get unsubscribed.
     * <p>
     * @return {@code HystrixProperty<Integer>}
     */
    public HystrixProperty<Integer> executionTimeoutInMilliseconds() {
        /**
         * Calling a deprecated method here is a temporary workaround. We do
         * this because {@link #executionTimeoutInMilliseconds()} is a new
         * method (as of 1.4.0-rc.7) and an extending class will not have this
         * method. It will have
         * {@link #executionIsolationThreadTimeoutInMilliseconds()}, however.
         * So, to stay compatible with an extension, we perform this redirect.
         */
        return executionIsolationThreadTimeoutInMilliseconds();
    }

    /**
     * Whether the timeout mechanism is enabled for this command
     * @return {@code HystrixProperty<Boolean>}
     * @since 1.4.4
     */
    public HystrixProperty<Boolean> executionTimeoutEnabled() {
        return executionTimeoutEnabled;
    }

    /**
     * Number of concurrent requests permitted to
     * {@link HystrixCommand#getFallback()}. Requests beyond the concurrent
     * limit will fail-fast and not attempt retrieving a fallback.
     * @return {@code HystrixProperty<Integer>}
     */
    public HystrixProperty<Integer> fallbackIsolationSemaphoreMaxConcurrentRequests() {
        return fallbackIsolationSemaphoreMaxConcurrentRequests;
    }

    /**
     * Whether {@link HystrixCommand#getFallback()} should be attempted when
     * failure occurs.
     * @return {@code HystrixProperty<Boolean>}
     * @since 1.2
     */
    public HystrixProperty<Boolean> fallbackEnabled() {
        return fallbackEnabled;
    }

    /**
     * Time in milliseconds to wait between allowing health snapshots to be
     * taken that calculate success and error percentages and affect
     * {@link HystrixCircuitBreaker#isOpen()} status.
     * <p>
     * On high-volume circuits the continual calculation of error percentage can
     * become CPU intensive thus this controls how often it is calculated.
     * @return {@code HystrixProperty<Integer>}
     */
    public HystrixProperty<Integer> metricsHealthSnapshotIntervalInMilliseconds() {
        return metricsHealthSnapshotIntervalInMilliseconds;
    }

    /**
     * Maximum number of values stored in each bucket of the rolling percentile.
     * This is passed into {@link HystrixRollingPercentile} inside
     * {@link HystrixCommandMetrics}.
     * @return {@code HystrixProperty<Integer>}
     */
    public HystrixProperty<Integer> metricsRollingPercentileBucketSize() {
        return metricsRollingPercentileBucketSize;
    }

    /**
     * Whether percentile metrics should be captured using
     * {@link HystrixRollingPercentile} inside {@link HystrixCommandMetrics}.
     * @return {@code HystrixProperty<Boolean>}
     */
    public HystrixProperty<Boolean> metricsRollingPercentileEnabled() {
        return metricsRollingPercentileEnabled;
    }

    /**
     * Duration of percentile rolling window in milliseconds. This is passed
     * into {@link HystrixRollingPercentile} inside
     * {@link HystrixCommandMetrics}.
     * @return {@code HystrixProperty<Integer>}
     * @deprecated Use {@link #metricsRollingPercentileWindowInMilliseconds()}
     */
    public HystrixProperty<Integer> metricsRollingPercentileWindow() {
        return metricsRollingPercentileWindowInMilliseconds;
    }

    /**
     * Duration of percentile rolling window in milliseconds. This is passed
     * into {@link HystrixRollingPercentile} inside
     * {@link HystrixCommandMetrics}.
     * @return {@code HystrixProperty<Integer>}
     */
    public HystrixProperty<Integer> metricsRollingPercentileWindowInMilliseconds() {
        return metricsRollingPercentileWindowInMilliseconds;
    }

    /**
     * Number of buckets the rolling percentile window is broken into. This is
     * passed into {@link HystrixRollingPercentile} inside
     * {@link HystrixCommandMetrics}.
     * @return {@code HystrixProperty<Integer>}
     */
    public HystrixProperty<Integer> metricsRollingPercentileWindowBuckets() {
        return metricsRollingPercentileWindowBuckets;
    }

    /**
     * Duration of statistical rolling window in milliseconds. This is passed
     * into {@link HystrixRollingNumber} inside {@link HystrixCommandMetrics}.
     * @return {@code HystrixProperty<Integer>}
     */
    public HystrixProperty<Integer> metricsRollingStatisticalWindowInMilliseconds() {
        return metricsRollingStatisticalWindowInMilliseconds;
    }

    /**
     * Number of buckets the rolling statistical window is broken into. This is
     * passed into {@link HystrixRollingNumber} inside
     * {@link HystrixCommandMetrics}.
     * @return {@code HystrixProperty<Integer>}
     */
    public HystrixProperty<Integer> metricsRollingStatisticalWindowBuckets() {
        return metricsRollingStatisticalWindowBuckets;
    }

    /**
     * Whether {@link HystrixCommand#getCacheKey()} should be used with
     * {@link HystrixRequestCache} to provide de-duplication functionality via
     * request-scoped caching.
     * @return {@code HystrixProperty<Boolean>}
     */
    public HystrixProperty<Boolean> requestCacheEnabled() {
        return requestCacheEnabled;
    }

    /**
     * Whether {@link HystrixCommand} execution and events should be logged to
     * {@link HystrixRequestLog}.
     * @return {@code HystrixProperty<Boolean>}
     */
    public HystrixProperty<Boolean> requestLogEnabled() {
        return requestLogEnabled;
    }

    private HystrixProperty<ExecutionIsolationStrategy> getProperty(String propertyPrefix, String command,
            HystrixCommandKey key, String instanceProperty, ExecutionIsolationStrategy builderOverrideValue,
            ExecutionIsolationStrategy defaultValue) {
        return new ExecutionIsolationStrategyHystrixProperty(builderOverrideValue, key, propertyPrefix, command,
                defaultValue, instanceProperty);
    }

    /**
     * HystrixProperty that converts a String to ExecutionIsolationStrategy so
     * we remain TypeSafe.
     */
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
            property.addCallback(new Runnable() {

                @Override
                public void run() {
                    // when the property value changes we'll update the value
                    parseProperty();
                }

            });
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
