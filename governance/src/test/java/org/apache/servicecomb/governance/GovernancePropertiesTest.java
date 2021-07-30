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
import org.apache.servicecomb.governance.policy.RateLimitingPolicy;
import org.apache.servicecomb.governance.policy.RetryPolicy;
import org.apache.servicecomb.governance.properties.BulkheadProperties;
import org.apache.servicecomb.governance.properties.CircuitBreakerProperties;
import org.apache.servicecomb.governance.properties.GovernanceProperties;
import org.apache.servicecomb.governance.properties.MatchProperties;
import org.apache.servicecomb.governance.properties.RateLimitProperties;
import org.apache.servicecomb.governance.properties.RetryProperties;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/*.xml", initializers = ConfigFileApplicationContextInitializer.class)
public class GovernancePropertiesTest {

  @Autowired
  private List<GovernanceProperties<? extends AbstractPolicy>> propertiesList;

  @Autowired
  private MatchProperties matchProperties;

  @Autowired
  private BulkheadProperties bulkheadProperties;

  @Autowired
  private CircuitBreakerProperties circuitBreakerProperties;

  @Autowired
  private RateLimitProperties rateLimitProperties;

  @Autowired
  private RetryProperties retryProperties;

  @Autowired
  private Environment environment;

  private Map<String, Object> dynamicValues = new HashMap<>();

  private static final float DELTA = 0.0000001f;

  @Before
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

  @After
  public void tearDown() {
    Set<String> keys = dynamicValues.keySet();
    keys.forEach(k -> dynamicValues.put(k, null));
    GovernanceEventManager.post(new GovernanceConfigurationChangedEvent(new HashSet<>(dynamicValues.keySet())));
  }

  @Test
  public void test_all_bean_is_loaded() {
    Assert.assertEquals(4, propertiesList.size());
  }

  @Test
  public void test_match_properties_successfully_loaded() {
    Map<String, TrafficMarker> markers = matchProperties.getParsedEntity();
    Assert.assertEquals(4, markers.size());
    TrafficMarker demoRateLimiting = markers.get("demo-rateLimiting");
    List<Matcher> matchers = demoRateLimiting.getMatches();
    Assert.assertEquals(1, matchers.size());
    Matcher matcher = matchers.get(0);
    Assert.assertEquals("/hello", matcher.getApiPath().get("exact"));

    TrafficMarker demoBulkhead = markers.get("demo-bulkhead");
    matchers = demoBulkhead.getMatches();
    Assert.assertEquals(2, matchers.size());
    matcher = matchers.get(0);
    Assert.assertEquals("/bulkhead", matcher.getApiPath().get("exact"));
    Assert.assertEquals("matchPath", matcher.getName());
  }

  @Test
  public void test_match_properties_delete() {
    Map<String, TrafficMarker> markers = matchProperties.getParsedEntity();
    Assert.assertEquals(4, markers.size());
    dynamicValues.put("servicecomb.matchGroup.test", "matches:\n"
        + "  - apiPath:\n"
        + "      exact: \"/hello2\"\n"
        + "    name: match0");
    GovernanceEventManager.post(new GovernanceConfigurationChangedEvent(new HashSet<>(dynamicValues.keySet())));
    markers = matchProperties.getParsedEntity();
    Assert.assertEquals(5, markers.size());
    tearDown();
    markers = matchProperties.getParsedEntity();
    Assert.assertEquals(4, markers.size());
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
    Assert.assertEquals(5, markers.size());
    TrafficMarker demoRateLimiting = markers.get("demo-rateLimiting");
    List<Matcher> matchers = demoRateLimiting.getMatches();
    Assert.assertEquals(1, matchers.size());
    Matcher matcher = matchers.get(0);
    Assert.assertEquals("/hello2", matcher.getApiPath().get("exact"));

    demoRateLimiting = markers.get("demo-rateLimiting2");
    matchers = demoRateLimiting.getMatches();
    Assert.assertEquals(1, matchers.size());
    matcher = matchers.get(0);
    Assert.assertEquals("/hello2", matcher.getApiPath().get("exact"));
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
    Assert.assertEquals(2, policies.size());
    BulkheadPolicy policy = policies.get("demo-bulkhead");
    Assert.assertEquals(2, policy.getMaxConcurrentCalls());
    Assert.assertEquals(2000, Duration.parse(policy.getMaxWaitDuration()).toMillis());

    policies = bulkheadProperties.getParsedEntity();
    Assert.assertEquals(2, policies.size());
    policy = policies.get("bulkhead1");
    Assert.assertEquals(3, policy.getMaxConcurrentCalls());
    Assert.assertEquals(3000, Duration.parse(policy.getMaxWaitDuration()).toMillis());
    Assert.assertEquals("bulkhead1", policy.getName());
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

    EventManager.post(new ConfigurationChangedEvent(new HashSet<>(dynamicValues.keySet())));

    Map<String, BulkheadPolicy> policies = bulkheadProperties.getParsedEntity();
    Assert.assertEquals(4, policies.size());
    BulkheadPolicy policy = policies.get("test-bulkhead1");
    Assert.assertEquals(0, policy.getMaxConcurrentCalls());
    Assert.assertEquals(2000, Duration.parse(policy.getMaxWaitDuration()).toMillis());

    policy = policies.get("test-bulkhead2");
    Assert.assertEquals(1000, policy.getMaxConcurrentCalls());
    Assert.assertEquals(0, Duration.parse(policy.getMaxWaitDuration()).toMillis());
    Assert.assertEquals("test-bulkhead2", policy.getName());

    policy = policies.get("test-bulkhead3");
    Assert.assertEquals(0, policy.getMaxConcurrentCalls());
    Assert.assertEquals(2000, Duration.parse(policy.getMaxWaitDuration()).toMillis());
    Assert.assertEquals("test-bulkhead3", policy.getName());
  }

  @Test
  public void test_bulkhead_properties_successfully_loaded() {
    Map<String, BulkheadPolicy> policies = bulkheadProperties.getParsedEntity();
    Assert.assertEquals(1, policies.size());
    BulkheadPolicy policy = policies.get("demo-bulkhead");
    Assert.assertEquals(1, policy.getMaxConcurrentCalls());
    Assert.assertEquals(3000, Duration.parse(policy.getMaxWaitDuration()).toMillis());
  }

  @Test
  public void test_circuit_breaker_properties_successfully_loaded() {
    Map<String, CircuitBreakerPolicy> policies = circuitBreakerProperties.getParsedEntity();
    Assert.assertEquals(1, policies.size());
    CircuitBreakerPolicy policy = policies.get("demo-circuitBreaker");
    Assert.assertEquals(2, policy.getMinimumNumberOfCalls());
    Assert.assertEquals("2", policy.getSlidingWindowSize());
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
    Assert.assertEquals(4, policies.size());
    CircuitBreakerPolicy policy = policies.get("name1");
    Assert.assertEquals("count", policy.getSlidingWindowType());
    Assert.assertEquals("2", policy.getSlidingWindowSize());

    policy = policies.get("name2");
    Assert.assertEquals("time", policy.getSlidingWindowType());
    Assert.assertEquals("2", policy.getSlidingWindowSize());

    policy = policies.get("name3");
    Assert.assertEquals("60", policy.getSlidingWindowSize());
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
    Assert.assertEquals(20.0f, policy.getFailureRateThreshold(), DELTA);
    Assert.assertEquals(100.0f, policy.getSlowCallRateThreshold(), DELTA);

    policy = policies.get("type2");
    Assert.assertEquals(20.33f, policy.getFailureRateThreshold(), DELTA);
    Assert.assertEquals(0.01f, policy.getSlowCallRateThreshold(), DELTA);
  }

  @Test
  public void test_rate_limit_properties_successfully_loaded() {
    Map<String, RateLimitingPolicy> policies = rateLimitProperties.getParsedEntity();
    Assert.assertEquals(1, policies.size());
    RateLimitingPolicy policy = policies.get("demo-rateLimiting");
    Assert.assertEquals(1, policy.getRate());
  }

  @Test
  public void test_retry_properties_successfully_loaded() {
    Map<String, RetryPolicy> policies = retryProperties.getParsedEntity();
    Assert.assertEquals(1, policies.size());
    RetryPolicy policy = policies.get("demo-retry");
    Assert.assertEquals(3, policy.getMaxAttempts());
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
    Assert.assertEquals(3, policies.size());
    BulkheadPolicy policy = policies.get("test1");
    Assert.assertEquals(2000, Duration.parse(policy.getMaxWaitDuration()).toMillis());

    policies = bulkheadProperties.getParsedEntity();
    Assert.assertEquals(3, policies.size());
    policy = policies.get("test2");
    Assert.assertEquals(60000, Duration.parse(policy.getMaxWaitDuration()).toMillis());
  }
}
