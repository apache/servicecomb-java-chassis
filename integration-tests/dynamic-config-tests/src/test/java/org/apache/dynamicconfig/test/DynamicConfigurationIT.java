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

package org.apache.dynamicconfig.test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.netflix.config.DynamicPropertyFactory;

public class DynamicConfigurationIT {
  private static String url;
  private static String token;
  private static final HttpHeaders headers = new HttpHeaders();
  private static final RestTemplate rest = new RestTemplate();

  @BeforeClass
  public static void setUp() throws Exception {
    Log4jUtils.init();
    BeanUtils.init();
    url = DynamicPropertyFactory.getInstance().getStringProperty("apollo.config.serverUri", "missing").getValue();
    token = DynamicPropertyFactory.getInstance().getStringProperty("apollo.config.token", "missing").getValue();
    headers.add("Content-Type", "application/json;charset=UTF-8");
    headers.add("Authorization", token);
  }

  @After
  public void tearDown() {
    clearConfiguration();
  }

  public int clearConfiguration() {
    String delete = url + "/openapi/v1/envs/DEV/apps/SampleApp/clusters/default/namespaces/application/items/loadbalance?operator=apollo";
    HttpEntity<?> entity = new HttpEntity<Object>(headers);
    ResponseEntity<String> exchange = rest.exchange(delete, HttpMethod.DELETE, entity, String.class);
    Assert.assertEquals(exchange.getStatusCodeValue(), HttpStatus.OK);
    return releaseConfiguration();
  }

  public int releaseConfiguration(){
    String release = url + "/openapi/v1/envs/DEV/apps/SampleApp/clusters/default/namespaces/application/releases";
    RestTemplate rest = new RestTemplate();
    Map<String, String> body = new HashMap<>();
    body.put("releaseTitle", "release-configuration");
    body.put("releasedBy", "apollo");
    HttpEntity<?> entity = new HttpEntity<Object>(body, headers);
    ResponseEntity<String> exchange = rest.exchange(release, HttpMethod.POST, entity, String.class);
    return exchange.getStatusCodeValue();
  }

  @Test
  public void testDynamicConfiguration() {
    //before
    Assert.assertEquals(DynamicPropertyFactory.getInstance().getStringProperty("loadbalcance", "default").getValue(), "default");

    String setLoadBalance = url + "/openapi/v1/envs/DEV/apps/SampleApp/clusters/default/namespaces/application/items";
    Map<String, String> body = new HashMap<>();
    body.put("key", "loadbalance");
    body.put("value", "roundrobbin");
    body.put("dataChangeCreatedBy", "apollo");
    HttpEntity<?> entity = new HttpEntity<Object>(body, headers);
    ResponseEntity<String> exchange = rest.exchange(setLoadBalance, HttpMethod.POST, entity, String.class);
    Assert.assertEquals(exchange.getStatusCodeValue(), HttpStatus.OK.value());
    Assert.assertEquals(releaseConfiguration(), HttpStatus.OK);

    await().atMost(5, SECONDS).until(
        () -> DynamicPropertyFactory.getInstance().getStringProperty("loadbalance", "default").getValue()
            .equals("roundrobbin"));

    String updateLoadBalance =
        url + "/openapi/v1/envs/DEV/apps/SampleApp/clusters/default/namespaces/application/items/" + "loadbalance";
    body.clear();
    body.put("key", "loadbalance");
    body.put("value", "random");
    body.put("dataChangeLastModifiedBy", "apollo");
    entity = new HttpEntity<Object>(body, headers);
    exchange = rest.exchange(updateLoadBalance, HttpMethod.PUT, entity, String.class);
    Assert.assertEquals(exchange.getStatusCodeValue(), HttpStatus.OK.value());
    Assert.assertEquals(releaseConfiguration(), HttpStatus.OK);

    await().atMost(5, SECONDS).until(
        () -> DynamicPropertyFactory.getInstance().getStringProperty("loadbalance", "default").getValue()
            .equals("random"));
  }
}
