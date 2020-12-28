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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import org.apache.servicecomb.governance.event.ConfigurationChangedEvent;
import org.apache.servicecomb.governance.event.EventManager;
import org.apache.servicecomb.governance.marker.Matcher;
import org.apache.servicecomb.governance.marker.TrafficMarker;
import org.apache.servicecomb.governance.policy.AbstractPolicy;
import org.apache.servicecomb.governance.policy.BulkheadPolicy;
import org.apache.servicecomb.governance.policy.CircuitBreakerPolicy;
import org.apache.servicecomb.governance.policy.RateLimitingPolicy;
import org.apache.servicecomb.governance.policy.RetryPolicy;
import org.apache.servicecomb.governance.properties.BulkheadProperties;
import org.apache.servicecomb.governance.properties.CircuitBreakerProperties;
import org.apache.servicecomb.governance.properties.GovProperties;
import org.apache.servicecomb.governance.properties.MatchProperties;
import org.apache.servicecomb.governance.properties.RateLimitProperties;
import org.apache.servicecomb.governance.properties.RetryProperties;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/*.xml", initializers = ConfigFileApplicationContextInitializer.class)
public class GovPropertiesTest {

  @Autowired
  private List<GovProperties<? extends AbstractPolicy>> propertiesList;

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

  public GovPropertiesTest() {
    System.out.print(1);
  }

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
    EventManager.post(new ConfigurationChangedEvent(new ArrayList<>(dynamicValues.keySet())));
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
    Assert.assertEquals("match0", matcher.getName());
    Assert.assertEquals("/hello", matcher.getApiPath().get("exact"));
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

    EventManager.post(new ConfigurationChangedEvent(new ArrayList<>(dynamicValues.keySet())));

    Map<String, TrafficMarker> markers = matchProperties.getParsedEntity();
    Assert.assertEquals(5, markers.size());
    TrafficMarker demoRateLimiting = markers.get("demo-rateLimiting");
    List<Matcher> matchers = demoRateLimiting.getMatches();
    Assert.assertEquals(1, matchers.size());
    Matcher matcher = matchers.get(0);
    Assert.assertEquals("match0", matcher.getName());
    Assert.assertEquals("/hello2", matcher.getApiPath().get("exact"));

    demoRateLimiting = markers.get("demo-rateLimiting2");
    matchers = demoRateLimiting.getMatches();
    Assert.assertEquals(1, matchers.size());
    matcher = matchers.get(0);
    Assert.assertEquals("match0", matcher.getName());
    Assert.assertEquals("/hello2", matcher.getApiPath().get("exact"));
  }

  @Test
  public void test_bulkhead_properties_changed() {
    dynamicValues.put("servicecomb.bulkhead.bulkhead0", "rules:\n"
        + "  match: demo-bulkhead.xx\n"
        + "  precedence: 100\n"
        + "maxConcurrentCalls: 2\n"
        + "maxWaitDuration: 2000");
    dynamicValues.put("servicecomb.bulkhead.bulkhead1", "rules:\n"
        + "  match: demo-bulkhead.xx\n"
        + "  precedence: 100\n"
        + "maxConcurrentCalls: 3\n"
        + "maxWaitDuration: 3000");

    EventManager.post(new ConfigurationChangedEvent(new ArrayList<>(dynamicValues.keySet())));

    Map<String, BulkheadPolicy> policies = bulkheadProperties.getParsedEntity();
    Assert.assertEquals(2, policies.size());
    BulkheadPolicy policy = policies.get("bulkhead0");
    Assert.assertEquals(2, policy.getMaxConcurrentCalls());
    Assert.assertEquals(2000, policy.getMaxWaitDuration());
    Assert.assertEquals("demo-bulkhead.xx", policy.getRules().getMatch());
    Assert.assertEquals(100, policy.getRules().getPrecedence());

    policies = bulkheadProperties.getParsedEntity();
    Assert.assertEquals(2, policies.size());
    policy = policies.get("bulkhead1");
    Assert.assertEquals(3, policy.getMaxConcurrentCalls());
    Assert.assertEquals(3000, policy.getMaxWaitDuration());
    Assert.assertEquals("demo-bulkhead.xx", policy.getRules().getMatch());
    Assert.assertEquals(100, policy.getRules().getPrecedence());
  }

  @Test
  public void test_bulkhead_properties_successfully_loaded() {
    Map<String, BulkheadPolicy> policies = bulkheadProperties.getParsedEntity();
    Assert.assertEquals(1, policies.size());
    BulkheadPolicy policy = policies.get("bulkhead0");
    Assert.assertEquals(1, policy.getMaxConcurrentCalls());
    Assert.assertEquals(3000, policy.getMaxWaitDuration());
    Assert.assertEquals("demo-bulkhead.xx", policy.getRules().getMatch());
    Assert.assertEquals(100, policy.getRules().getPrecedence());
  }

  @Test
  public void test_circuit_breaker_properties_successfully_loaded() {
    Map<String, CircuitBreakerPolicy> policies = circuitBreakerProperties.getParsedEntity();
    Assert.assertEquals(1, policies.size());
    CircuitBreakerPolicy policy = policies.get("circuitBreaker0");
    Assert.assertEquals(2, policy.getMinimumNumberOfCalls());
    Assert.assertEquals(2, policy.getSlidingWindowSize());
    Assert.assertEquals("demo-circuitBreaker.xx", policy.getRules().getMatch());
    Assert.assertEquals(0, policy.getRules().getPrecedence());
  }

  @Test
  public void test_rate_limit_properties_successfully_loaded() {
    Map<String, RateLimitingPolicy> policies = rateLimitProperties.getParsedEntity();
    Assert.assertEquals(1, policies.size());
    RateLimitingPolicy policy = policies.get("rateLimiting0");
    Assert.assertEquals(1, policy.getRate());
    Assert.assertEquals("demo-rateLimiting.match0", policy.getRules().getMatch());
    Assert.assertEquals(0, policy.getRules().getPrecedence());
  }

  @Test
  public void test_retry_properties_successfully_loaded() {
    Map<String, RetryPolicy> policies = retryProperties.getParsedEntity();
    Assert.assertEquals(1, policies.size());
    RetryPolicy policy = policies.get("retry0");
    Assert.assertEquals(3, policy.getMaxAttempts());
    Assert.assertEquals("demo-retry.xx", policy.getRules().getMatch());
    Assert.assertEquals(0, policy.getRules().getPrecedence());
  }
}
