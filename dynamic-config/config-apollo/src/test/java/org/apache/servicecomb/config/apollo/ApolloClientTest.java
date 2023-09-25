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

package org.apache.servicecomb.config.apollo;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.apollo.ApolloClient.ConfigRefresh;
import org.apache.servicecomb.config.apollo.ApolloDynamicPropertiesSource.UpdateHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ApolloClientTest {
  @BeforeAll
  public static void setUpClass() {

    ApolloConfig.setConcurrentCompositeConfiguration(ConfigUtil.createLocalConfig());
  }

  @Test
  public void refreshApolloConfig() {
    ApolloConfig apolloConfig = ApolloConfig.INSTANCE;
    RestTemplate rest = Mockito.mock(RestTemplate.class);
    ApolloClient.setRest(rest);

    ResponseEntity<String> responseEntity = new ResponseEntity<>(
        "{\"apollo\":\"mocked\", \"configurations\":{\"timeout\":1000}}", HttpStatus.OK);
    Mockito.when(rest.exchange(
        ArgumentMatchers.anyString(),
        ArgumentMatchers.any(HttpMethod.class),
        ArgumentMatchers.<HttpEntity<String>>any(),
        ArgumentMatchers.<Class<String>>any())).thenReturn(responseEntity);
    ApolloDynamicPropertiesSource impl = new ApolloDynamicPropertiesSource();
    UpdateHandler updateHandler = impl.new UpdateHandler();
    ApolloClient apolloClient = new ApolloClient(updateHandler);
    ConfigRefresh cr = apolloClient.new ConfigRefresh(apolloConfig.getServerUri());
    cr.run();

    Assertions.assertEquals(1, ApolloClient.getOriginalConfigMap().size());
  }

  @Test
  public void testCompareChangedConfig() {
    boolean status = true;
    Map<String, Object> before = new HashMap<>();
    Map<String, Object> after = new HashMap<>();

    ApolloDynamicPropertiesSource impl = new ApolloDynamicPropertiesSource();
    UpdateHandler updateHandler = impl.new UpdateHandler();
    ApolloClient apolloClient = new ApolloClient(updateHandler);

    ConfigRefresh cr = apolloClient.new ConfigRefresh("");

    try {
      cr.compareChangedConfig(before, after);
    } catch (Exception e) {
      status = false;
    }
    Assertions.assertTrue(status);

    before.put("test", "testValue");
    try {
      cr.compareChangedConfig(before, after);
    } catch (Exception e) {
      status = false;
    }
    Assertions.assertTrue(status);

    after.put("test", "testValue2");
    try {
      cr.compareChangedConfig(before, after);
    } catch (Exception e) {
      status = false;
    }
    Assertions.assertTrue(status);

    try {
      cr.compareChangedConfig(before, after);
    } catch (Exception e) {
      status = false;
    }
    Assertions.assertTrue(status);
  }
}
