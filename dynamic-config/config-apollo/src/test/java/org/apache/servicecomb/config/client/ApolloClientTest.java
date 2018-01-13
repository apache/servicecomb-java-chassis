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

package org.apache.servicecomb.config.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.archaius.sources.ApolloConfigurationSourceImpl;
import org.apache.servicecomb.config.archaius.sources.ApolloConfigurationSourceImpl.UpdateHandler;
import org.apache.servicecomb.config.client.ApolloClient.ConfigRefresh;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import mockit.Deencapsulation;

public class ApolloClientTest {
  @BeforeClass
  public static void setUpClass() {

    ApolloConfig.setConcurrentCompositeConfiguration(ConfigUtil.createLocalConfig());
  }

  @Test
  public void refreshApolloConfig() {
    ApolloConfig apolloConfig = ApolloConfig.INSTANCE;
    RestTemplate rest = Mockito.mock(RestTemplate.class);

    ResponseEntity<String> responseEntity = new ResponseEntity<>(
        "{\"apollo\":\"mocked\", \"configurations\":{\"timeout\":1000}}", HttpStatus.OK);
    Mockito.when(rest.exchange(
        Matchers.anyString(),
        Matchers.any(HttpMethod.class),
        Matchers.<HttpEntity<String>>any(),
        Matchers.<Class<String>>any())).thenReturn(responseEntity);
    ApolloConfigurationSourceImpl impl = new ApolloConfigurationSourceImpl();
    UpdateHandler updateHandler = impl.new UpdateHandler();
    ApolloClient apolloClient = new ApolloClient(updateHandler);
    Deencapsulation.setField(apolloClient, "rest", rest);
    ConfigRefresh cr = apolloClient.new ConfigRefresh(apolloConfig.getServerUri());
    cr.run();

    Map<String, Object> originMap = Deencapsulation.getField(apolloClient, "originalConfigMap");
    Assert.assertEquals(1, originMap.size());
  }

  @Test
  public void testCompareChangedConfig() {
    boolean status = true;
    Map<String, Object> before = new HashMap<>();
    Map<String, Object> after = new HashMap<>();

    ApolloConfigurationSourceImpl impl = new ApolloConfigurationSourceImpl();
    UpdateHandler updateHandler = impl.new UpdateHandler();
    ApolloClient apolloClient = new ApolloClient(updateHandler);

    ConfigRefresh cr = apolloClient.new ConfigRefresh("");

    try {
      Deencapsulation.invoke(cr, "compareChangedConfig", before, after);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);

    status = true;
    before.put("test", "testValue");
    try {
      Deencapsulation.invoke(cr, "compareChangedConfig", before, after);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);

    status = true;
    after.put("test", "testValue2");
    try {
      Deencapsulation.invoke(cr, "compareChangedConfig", before, after);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);

    status = true;
    try {
      Deencapsulation.invoke(cr, "compareChangedConfig", before, after);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);
  }
}
