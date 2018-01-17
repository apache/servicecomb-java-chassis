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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

public class DynamicConfigurationIT {


  public static String url;
  public static String token;

  @BeforeClass
  public static void setUp() throws Exception {
    Log4jUtils.init();
    BeanUtils.init();
    url = DynamicPropertyFactory.getInstance().getStringProperty("apollo.config.serverUri", "missing").getValue();
    token = DynamicPropertyFactory.getInstance().getStringProperty("apollo.config.token", "missing").getValue();
  }

  @After
  public void tearDown() throws Exception {
    //delete
    clearConfiguration();
  }

  //delete configuration items set by test code
  public void clearConfiguration() {
    String delete = url + "/openapi/v1/envs/DEV/apps/SampleApp/clusters/default/namespaces/application/items/loadbalance?operator=apollo";
    String release = url + "/openapi/v1/envs/DEV/apps/SampleApp/clusters/default/namespaces/application/releases";
    RestTemplate rest = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json;charset=UTF-8");
    headers.add("Authorization", token);
    HttpEntity<?> entity = new HttpEntity<Object>(headers);

    ResponseEntity<String> exchange = rest.exchange(delete, HttpMethod.DELETE, entity, String.class);
    Map<String,String> body = new HashMap<>();
    body.put("releaseTitle", "release-configuration");
    body.put("releasedBy", "apollo");
    entity = new HttpEntity<Object>(body, headers);
    exchange = rest.exchange(release, HttpMethod.POST, entity, String.class);

  }

  @Test
  public void testDynamicConfiguration() {
    //before
    Assert.assertEquals(DynamicPropertyFactory.getInstance().getStringProperty("loadbalcance", "default").getValue(), "default");

    //set and return 200. release 200,update return 200

    String setLoadBalance = url + "/openapi/v1/envs/DEV/apps/SampleApp/clusters/default/namespaces/application/items";

    String release = url + "/openapi/v1/envs/DEV/apps/SampleApp/clusters/default/namespaces/application/releases";
    RestTemplate rest = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json;charset=UTF-8");
    headers.add("Authorization", token);

    //set new configuration item
    Map<String, String> body = new HashMap<>();
    body.put("key", "loadbalance");
    body.put("value", "roundrobbin");
    body.put("dataChangeCreatedBy", "apollo");
    HttpEntity<?> entity = new HttpEntity<Object>(body, headers);

    ResponseEntity<String> exchange = rest.exchange(setLoadBalance, HttpMethod.POST, entity, String.class);
    Assert.assertEquals(exchange.getStatusCodeValue(), HttpStatus.OK.value());

    //relese loadbalance item
    body.clear();
    body.put("releaseTitle", "release-configuration");
    body.put("releasedBy", "apollo");
    entity = new HttpEntity<Object>(body, headers);
    exchange = rest.exchange(release, HttpMethod.POST, entity, String.class);
    Assert.assertEquals(exchange.getStatusCodeValue(), HttpStatus.OK.value());

    //waiting for a refresh cycle
    await().atMost(5, SECONDS).until(
        () -> DynamicPropertyFactory.getInstance().getStringProperty("loadbalance", "default").getValue()
            .equals("roundrobbin"));

    //update loadbalance value
    String updateLoadBalance =
        url + "/openapi/v1/envs/DEV/apps/SampleApp/clusters/default/namespaces/application/items/" + "loadbalance";
    body.clear();
    body.put("key", "loadbalance");
    body.put("value", "random");
    body.put("dataChangeLastModifiedBy", "apollo");
    entity = new HttpEntity<Object>(body, headers);
    exchange = rest.exchange(updateLoadBalance, HttpMethod.PUT, entity, String.class);
    Assert.assertEquals(exchange.getStatusCodeValue(), HttpStatus.OK.value());

    //release again
    body.clear();
    body.put("releaseTitle", "test-release");
    body.put("releasedBy", "apollo");
    entity = new HttpEntity<Object>(body, headers);
    exchange = rest.exchange(release, HttpMethod.POST, entity, String.class);
    Assert.assertEquals(exchange.getStatusCodeValue(), HttpStatus.OK.value());
    await().atMost(5, SECONDS).until(
        () -> DynamicPropertyFactory.getInstance().getStringProperty("loadbalance", "default").getValue()
            .equals("random"));
  }
}
