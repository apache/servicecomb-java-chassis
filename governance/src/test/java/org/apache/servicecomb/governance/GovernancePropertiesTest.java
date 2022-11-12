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

package org.apache.servicecomb.governance;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.governance.event.GovernanceConfigurationChangedEvent;
import org.apache.servicecomb.governance.event.GovernanceEventManager;
import org.apache.servicecomb.governance.marker.Matcher;
import org.apache.servicecomb.governance.marker.TrafficMarker;
import org.apache.servicecomb.governance.policy.AbstractPolicy;
import org.apache.servicecomb.governance.policy.BulkheadPolicy;
import org.apache.servicecomb.governance.policy.CircuitBreakerPolicy;
import org.apache.servicecomb.governance.policy.FaultInjectionPolicy;
import org.apache.servicecomb.governance.policy.GovernanceCachePolicy;
import org.apache.servicecomb.governance.policy.RateLimitingPolicy;
import org.apache.servicecomb.governance.policy.RetryPolicy;
import org.apache.servicecomb.governance.policy.TimeLimiterPolicy;
import org.apache.servicecomb.governance.processor.injection.FaultInjectionConst;
import org.apache.servicecomb.governance.properties.BulkheadProperties;
import org.apache.servicecomb.governance.properties.CircuitBreakerProperties;
import org.apache.servicecomb.governance.properties.FaultInjectionProperties;
import org.apache.servicecomb.governance.properties.GovernanceCacheProperties;
import org.apache.servicecomb.governance.properties.GovernanceProperties;
import org.apache.servicecomb.governance.properties.InstanceIsolationProperties;
import org.apache.servicecomb.governance.properties.MatchProperties;
import org.apache.servicecomb.governance.properties.RateLimitProperties;
import org.apache.servicecomb.governance.properties.RetryProperties;
import org.apache.servicecomb.governance.properties.TimeLimiterProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {GovernanceConfiguration.class, MockConfiguration.class})
public class GovernancePropertiesTest {
  private List<GovernanceProperties<? extends AbstractPolicy>> propertiesList;

  private MatchProperties matchProperties;

  private BulkheadProperties bulkheadProperties;

  private CircuitBreakerProperties circuitBreakerProperties;

  private TimeLimiterProperties timeLimiterProperties;

  private GovernanceCacheProperties governanceCacheProperties;

  private InstanceIsolationProperties instanceIsolationProperties;

  private RateLimitProperties rateLimitProperties;

  private RetryProperties retryProperties;

  private FaultInjectionProperties faultInjectionProperties;

  private Environment environment;

  @Autowired
  public void setPropertiesList(List<GovernanceProperties<? extends AbstractPolicy>> propertiesList) {
    this.propertiesList = propertiesList;
  }

  @Autowired
  public void setMatchProperties(MatchProperties matchProperties) {
    this.matchProperties = matchProperties;
  }

  @Autowired
  public void setBulkheadProperties(BulkheadProperties bulkheadProperties) {
    this.bulkheadProperties = bulkheadProperties;
  }

  @Autowired
  public void setCircuitBreakerProperties(CircuitBreakerProperties circuitBreakerProperties) {
    this.circuitBreakerProperties = circuitBreakerProperties;
  }

  @Autowired
  public void setTimeLimiterProperties(TimeLimiterProperties timeLimiterProperties) {
    this.timeLimiterProperties = timeLimiterProperties;
  }

  @Autowired
  public void setGovernanceCacheProperties(GovernanceCacheProperties governanceCacheProperties) {
    this.governanceCacheProperties = governanceCacheProperties;
  }

  @Autowired
  public void setRateLimitProperties(RateLimitProperties rateLimitProperties) {
    this.rateLimitProperties = rateLimitProperties;
  }

  @Autowired
  public void setInstanceIsolationProperties(InstanceIsolationProperties instanceIsolationProperties) {
    this.instanceIsolationProperties = instanceIsolationProperties;
  }

  @Autowired
  public void setRetryProperties(RetryProperties retryProperties) {
    this.retryProperties = retryProperties;
  }

  @Autowired
  public void setFaultInjectionProperties(
      FaultInjectionProperties faultInjectionProperties) {
    this.faultInjectionProperties = faultInjectionProperties;
  }

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Autowired
  public void setDynamicValues(Map<String, Object> dynamicValues) {
    this.dynamicValues = dynamicValues;
  }

  public GovernancePropertiesTest() {
  }

  private Map<String, Object> dynamicValues = new HashMap<>();

  private static final float DELTA = 0.0000001f;

  @BeforeEach
  public void setUp() {
    ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;

    if (configurableEnvironment.getPropertySources().contains("testDynamicChange")) {
      configurableEnvironment.getPropertySources().remove("testDynamicChange");
    }

    configurableEnvironment.getPropertySources()
        .addFirst(new EnumerablePropertySource<Map<String, Object>>("testDynamicChange", dynamicValues) {
          @Override
          public Object getProperty(String s) {
            return this.getSource().get(s);
          }

          @Override
          public String[] getPropertyNames() {
            return this.getSource().keySet().toArray(new String[0]);
          }
        });
  }

  @AfterEach
  public void tearDown() {
    Set<String> keys = dynamicValues.keySet();
    keys.forEach(k -> dynamicValues.put(k, null));
    GovernanceEventManager.post(new GovernanceConfigurationChangedEvent(new HashSet<>(dynamicValues.keySet())));
  }

  @Test
  public void test_all_bean_is_loaded() {
    Assertions.assertEquals(16, propertiesList.size());
  }

  @Test
  public void test_match_properties_successfully_loaded() {
    Map<String, TrafficMarker> markers = matchProperties.getParsedEntity();
    TrafficMarker demoRateLimiting = markers.get("demo-rateLimiting");
    List<Matcher> matchers = demoRateLimiting.getMatches();
    Assertions.assertEquals(1, matchers.size());
    Matcher matcher = matchers.get(0);
    Assertions.assertEquals("/hello", matcher.getApiPath().get("exact"));

    TrafficMarker demoBulkhead = markers.get("demo-bulkhead");
    matchers = demoBulkhead.getMatches();
    Assertions.assertEquals(2, matchers.size());
    matcher = matchers.get(0);
    Assertions.assertEquals("/bulkhead", matcher.getApiPath().get("exact"));
    Assertions.assertEquals("matchPath", matcher.getName());
  }

  @Test
  public void test_match_properties_delete() {
    Map<String, TrafficMarker> markers = matchProperties.getParsedEntity();
    Assertions.assertEquals(null, markers.get("test"));
    dynamicValues.put("servicecomb.matchGroup.test", "matches:\n"
        + "  - apiPath:\n"
        + "      exact: \"/hello2\"\n"
        + "    name: match0");
    GovernanceEventManager.post(new GovernanceConfigurationChangedEvent(new HashSet<>(dynamicValues.keySet())));
    markers = matchProperties.getParsedEntity();
    Assertions.assertEquals(1, markers.get("test").getMatches().size());
    tearDown();
    markers = matchProperties.getParsedEntity();
    Assertions.assertEquals(null, markers.get("test"));
  }

  @Test
  public void test_match_properties_changed() {
    dynamicValues.put("servicecomb.matchGroup.demo-rateLimiting", "matches:\n"
        + "  - apiPath:\n"
        + "      exact: \"/hello2\"\n"
        + "    name: match0");
    dynamicValues.put("servicecomb.matchGroup.demo-rateLimiting2", "matches:\n"
        + "  - apiPath:\n"
        + "      exact: \"/hello2\"\n"
        + "    name: match0");

    GovernanceEventManager.post(new GovernanceConfigurationChangedEvent(new HashSet<>(dynamicValues.keySet())));

    Map<String, TrafficMarker> markers = matchProperties.getParsedEntity();
    TrafficMarker demoRateLimiting = markers.get("demo-rateLimiting");
    List<Matcher> matchers = demoRateLimiting.getMatches();
    Assertions.assertEquals(1, matchers.size());
    Matcher matcher = matchers.get(0);
    Assertions.assertEquals("/hello2", matcher.getApiPath().get("exact"));

    demoRateLimiting = markers.get("demo-rateLimiting2");
    matchers = demoRateLimiting.getMatches();
    Assertions.assertEquals(1, matchers.size());
    matcher = matchers.get(0);
    Assertions.assertEquals("/hello2", matcher.getApiPath().get("exact"));
  }

  @Test
  public void test_bulkhead_properties_changed() {
    dynamicValues.put("servicecomb.bulkhead.demo-bulkhead", "rules:\n"
        + "maxConcurrentCalls: 2\n"
        + "maxWaitDuration: 2000");
    dynamicValues.put("servicecomb.bulkhead.bulkhead1", "rules:\n"
        + "maxConcurrentCalls: 3\n"
        + "maxWaitDuration: 3000");

    GovernanceEventManager.post(new GovernanceConfigurationChangedEvent(new HashSet<>(dynamicValues.keySet())));

    Map<String, BulkheadPolicy> policies = bulkheadProperties.getParsedEntity();
    Assertions.assertEquals(3, policies.size());
    BulkheadPolicy policy = policies.get("demo-bulkhead");
    Assertions.assertEquals(2, policy.getMaxConcurrentCalls());
    Assertions.assertEquals(2000, Duration.parse(policy.getMaxWaitDuration()).toMillis());

    policies = bulkheadProperties.getParsedEntity();
    Assertions.assertEquals(3, policies.size());
    policy = policies.get("bulkhead1");
    Assertions.assertEquals(3, policy.getMaxConcurrentCalls());
    Assertions.assertEquals(3000, Duration.parse(policy.getMaxWaitDuration()).toMillis());
    Assertions.assertEquals("bulkhead1", policy.getName());
  }

  @Test
  public void test_bulkhead_properties_bound() {
    dynamicValues.put("servicecomb.bulkhead.test-bulkhead1", "rules:\n"
        + "maxConcurrentCalls: 0\n"
        + "maxWaitDuration: 2000");
    dynamicValues.put("servicecomb.bulkhead.test-bulkhead2", "rules:\n"
        + "maxConcurrentCalls: 1000\n"
        + "maxWaitDuration: 0");
    dynamicValues.put("servicecomb.bulkhead.test-bulkhead3", "rules:\n"
        + "maxConcurrentCalls: 0\n"
        + "maxWaitDuration: 2S");

    GovernanceEventManager.post(new GovernanceConfigurationChangedEvent(new HashSet<>(dynamicValues.keySet())));

    Map<String, BulkheadPolicy> policies = bulkheadProperties.getParsedEntity();
    Assertions.assertEquals(5, policies.size());
    BulkheadPolicy policy = policies.get("test-bulkhead1");
    Assertions.assertEquals(0, policy.getMaxConcurrentCalls());
    Assertions.assertEquals(2000, Duration.parse(policy.getMaxWaitDuration()).toMillis());

    policy = policies.get("test-bulkhead2");
    Assertions.assertEquals(1000, policy.getMaxConcurrentCalls());
    Assertions.assertEquals(0, Duration.parse(policy.getMaxWaitDuration()).toMillis());
    Assertions.assertEquals("test-bulkhead2", policy.getName());

    policy = policies.get("test-bulkhead3");
    Assertions.assertEquals(0, policy.getMaxConcurrentCalls());
    Assertions.assertEquals(2000, Duration.parse(policy.getMaxWaitDuration()).toMillis());
    Assertions.assertEquals("test-bulkhead3", policy.getName());
  }

  @Test
  public void test_bulkhead_properties_successfully_loaded() {
    Map<String, BulkheadPolicy> policies = bulkheadProperties.getParsedEntity();
    Assertions.assertEquals(2, policies.size());
    BulkheadPolicy policy = policies.get("demo-bulkhead");
    Assertions.assertEquals(1, policy.getMaxConcurrentCalls());
    Assertions.assertEquals(3000, Duration.parse(policy.getMaxWaitDuration()).toMillis());
  }

  @Test
  public void test_timelimiter_properties_successfully_loaded() {
    Map<String, TimeLimiterPolicy> policies = timeLimiterProperties.getParsedEntity();
    Assertions.assertEquals(2, policies.size());
    TimeLimiterPolicy timeLimiterPolicy = policies.get("demo-timeLimiter-other");
    Assertions.assertEquals(2000, Duration.parse(timeLimiterPolicy.getTimeoutDuration()).toMillis());
    Assertions.assertEquals(false, timeLimiterPolicy.isCancelRunningFuture());
  }

  @Test
  public void test_timelimiter_properties_bound() {
    dynamicValues.put("servicecomb.timeLimiter.name1", "rules:\n"
        + "timeoutDuration: 5000\n"
        + "cancelRunningFuture: false");

    GovernanceEventManager.post(new GovernanceConfigurationChangedEvent(new HashSet<>(dynamicValues.keySet())));

    Map<String, TimeLimiterPolicy> policies = timeLimiterProperties.getParsedEntity();
    Assertions.assertEquals(3, policies.size());
    TimeLimiterPolicy policy = policies.get("name1");
    Assertions.assertEquals(false, policy.isCancelRunningFuture());
    Assertions.assertEquals(5000, Duration.parse(policy.getTimeoutDuration()).toMillis());
  }

  @Test
  public void test_governanceCache_properties_successfully_loaded() {
    Map<String, GovernanceCachePolicy> policies = governanceCacheProperties.getParsedEntity();
    Assertions.assertEquals(2, policies.size());
    GovernanceCachePolicy governanceCachePolicy = policies.get("demo-governanceCache-other");
    Assertions.assertEquals(15, governanceCachePolicy.getConcurrencyLevel());
    Assertions.assertEquals(50000, governanceCachePolicy.getMaximumSize());
    Assertions.assertEquals(666666, Duration.parse(governanceCachePolicy.getTtl()).toMillis());
  }

  @Test
  public void test_governanceCache_properties_bound() {
    dynamicValues.put("servicecomb.cache.name1", "rules:\n"
        + "ttl: 3000000\n"
        + "maximumSize: 2000\n"
        + "concurrencyLevel: 6");

    GovernanceEventManager.post(new GovernanceConfigurationChangedEvent(new HashSet<>(dynamicValues.keySet())));

    Map<String, GovernanceCachePolicy> policies = governanceCacheProperties.getParsedEntity();
    Assertions.assertEquals(3, policies.size());
    GovernanceCachePolicy policy = policies.get("name1");
    Assertions.assertEquals(3000000, Duration.parse(policy.getTtl()).toMillis());
    Assertions.assertEquals(2000, policy.getMaximumSize());
    Assertions.assertEquals(6, policy.getConcurrencyLevel());
  }

  @Test
  public void test_circuit_breaker_properties_successfully_loaded() {
    Map<String, CircuitBreakerPolicy> policies = circuitBreakerProperties.getParsedEntity();
    Assertions.assertEquals(1, policies.size());
    CircuitBreakerPolicy policy = policies.get("demo-circuitBreaker");
    Assertions.assertEquals(2, policy.getMinimumNumberOfCalls());
    Assertions.assertEquals("2", policy.getSlidingWindowSize());
  }

  @Test
  public void test_circuit_breaker_properties_Of_windows_size() {
    dynamicValues.put("servicecomb.circuitBreaker.name1", "rules:\n"
        + "slidingWindowType: count\n"
        + "slidingWindowSize: 2");
    dynamicValues.put("servicecomb.circuitBreaker.name2", "rules:\n"
        + "slidingWindowType: time\n"
        + "slidingWindowSize: 2");
    dynamicValues.put("servicecomb.circuitBreaker.name3", "rules:\n"
        + "slidingWindowType: test\n"
        + "slidingWindowSize: 1M");

    GovernanceEventManager.post(new GovernanceConfigurationChangedEvent(new HashSet<>(dynamicValues.keySet())));

    Map<String, CircuitBreakerPolicy> policies = circuitBreakerProperties.getParsedEntity();
    Assertions.assertEquals(4, policies.size());
    CircuitBreakerPolicy policy = policies.get("name1");
    Assertions.assertEquals("count", policy.getSlidingWindowType());
    Assertions.assertEquals("2", policy.getSlidingWindowSize());

    policy = policies.get("name2");
    Assertions.assertEquals("time", policy.getSlidingWindowType());
    Assertions.assertEquals("2", policy.getSlidingWindowSize());

    policy = policies.get("name3");
    Assertions.assertEquals("60", policy.getSlidingWindowSize());
  }

  @Test
  public void test_circuit_breaker_properties_Of_type() {
    dynamicValues.put("servicecomb.circuitBreaker.type1", "rules:\n"
        + "failureRateThreshold: 20\n"
        + "slowCallRateThreshold: 100");
    dynamicValues.put("servicecomb.circuitBreaker.type2", "rules:\n"
        + "failureRateThreshold: 20.33\n"
        + "slowCallRateThreshold: 0.01");

    GovernanceEventManager.post(new GovernanceConfigurationChangedEvent(new HashSet<>(dynamicValues.keySet())));

    Map<String, CircuitBreakerPolicy> policies = circuitBreakerProperties.getParsedEntity();
    CircuitBreakerPolicy policy = policies.get("type1");
    Assertions.assertEquals(20.0f, policy.getFailureRateThreshold(), DELTA);
    Assertions.assertEquals(100.0f, policy.getSlowCallRateThreshold(), DELTA);

    policy = policies.get("type2");
    Assertions.assertEquals(20.33f, policy.getFailureRateThreshold(), DELTA);
    Assertions.assertEquals(0.01f, policy.getSlowCallRateThreshold(), DELTA);
  }

  @Test
  public void test_rate_limit_properties_successfully_loaded() {
    Map<String, RateLimitingPolicy> policies = rateLimitProperties.getParsedEntity();
    Assertions.assertEquals(2, policies.size());
    RateLimitingPolicy policy = policies.get("demo-rateLimiting");
    Assertions.assertEquals(1, policy.getRate());
  }

  @Test
  public void test_retry_properties_successfully_loaded() {
    Map<String, RetryPolicy> policies = retryProperties.getParsedEntity();
    Assertions.assertEquals(1, policies.size());
    RetryPolicy policy = policies.get("demo-retry");
    Assertions.assertEquals(3, policy.getMaxAttempts());
  }


  @Test
  public void test_properties_changed_to_duration() {
    dynamicValues.put("servicecomb.bulkhead.test1", "rules:\n"
        + "maxConcurrentCalls: 2\n"
        + "maxWaitDuration: 2S");
    dynamicValues.put("servicecomb.bulkhead.test2", "rules:\n"
        + "maxConcurrentCalls: 3\n"
        + "maxWaitDuration: 1M");

    GovernanceEventManager.post(new GovernanceConfigurationChangedEvent(new HashSet<>(dynamicValues.keySet())));

    Map<String, BulkheadPolicy> policies = bulkheadProperties.getParsedEntity();
    Assertions.assertEquals(4, policies.size());
    BulkheadPolicy policy = policies.get("test1");
    Assertions.assertEquals(2000, Duration.parse(policy.getMaxWaitDuration()).toMillis());

    policies = bulkheadProperties.getParsedEntity();
    Assertions.assertEquals(4, policies.size());
    policy = policies.get("test2");
    Assertions.assertEquals(60000, Duration.parse(policy.getMaxWaitDuration()).toMillis());
  }

  @Test
  public void test_instance_isolation_properties_successfully_loaded() {
    Map<String, CircuitBreakerPolicy> policies = instanceIsolationProperties.getParsedEntity();
    Assertions.assertEquals(1, policies.size());
    CircuitBreakerPolicy policy = policies.get("demo-allOperation");
    Assertions.assertEquals(2, policy.getMinimumNumberOfCalls());
    Assertions.assertEquals("2", policy.getSlidingWindowSize());
  }

  @Test
  public void test_fault_injection_properties_successfully_loaded() {
    Map<String, FaultInjectionPolicy> policies = faultInjectionProperties.getParsedEntity();
    Assertions.assertEquals(5, policies.size());
    FaultInjectionPolicy policy = policies.get("demo-faultInjectDelay");
    Assertions.assertEquals(FaultInjectionConst.TYPE_DELAY, policy.getType());
    Assertions.assertEquals(2000, policy.getDelayTimeToMillis());
    Assertions.assertEquals(100, policy.getPercentage());

    policy = policies.get("demo-faultInjectAbort");
    Assertions.assertEquals(FaultInjectionConst.TYPE_ABORT, policy.getType());
    Assertions.assertEquals(50, policy.getPercentage());
    Assertions.assertEquals(500, policy.getErrorCode());
  }
}
