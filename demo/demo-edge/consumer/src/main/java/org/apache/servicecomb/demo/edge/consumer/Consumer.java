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

package org.apache.servicecomb.demo.edge.consumer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.demo.edge.model.AppClientDataRsp;
import org.apache.servicecomb.demo.edge.model.ChannelRequestBase;
import org.apache.servicecomb.demo.edge.model.DependTypeA;
import org.apache.servicecomb.demo.edge.model.DependTypeB;
import org.apache.servicecomb.demo.edge.model.RecursiveSelfType;
import org.apache.servicecomb.demo.edge.model.ResultWithInstance;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

public class Consumer {
  RestTemplate template = RestTemplateBuilder.create();

  ChannelRequestBase request = new ChannelRequestBase();

  String edgePrefix;

  List<ResultWithInstance> addV1Result = new ArrayList<>();

  List<ResultWithInstance> decV1Result = new ArrayList<>();

  List<ResultWithInstance> addV2Result = new ArrayList<>();

  List<ResultWithInstance> decV2Result = new ArrayList<>();

  public Consumer() {
    request.setDeviceId("2a5cc42ff60006ac");
    request.setServiceToken(
        "c2VydmliZVRva2VuPTAwMjAwMDg2MDAwMDAwMDAwODMyYzYzamV1ZnV1cWdpYXgmRGV2aWNIVHIw"
            + "ZT0wJkRLdmljZUIEPTg2MzgoMDAyMDA0NDcwMiZhcHBJRD1jb20uaHVhd2VpLndhbGxldA");
    request.setPhoneType("VTR-AL00");
    request.setUserId("20086000000000832");
    request.setCmdId("5");
    request.setNet("1");
    request.setUserGrant("000");
    request.setSysVer("EMUI5.1");
    request.setTs("1497356427334");
    request.setChannelId("3");
    request.setLocation(null);
    request.setCmdVer("2.0");
    request.setLanguage("zh_CN");
  }

  public void run(String prefix) {
    prepareEdge(prefix);

    testRecursiveSelf();
    testDependType();
    testDownload();
    testDownloadBigFile();
    testErrorCode();

    invoke("/v1/add", 2, 1, addV1Result);
    invoke("/v1/add", 3, 1, addV1Result);
    invoke("/v1/add", 4, 1, addV1Result);
    invoke("/v1/add", 5, 1, addV1Result);

    invoke("/v1/dec", 2, 1, decV1Result);
    invoke("/v1/dec", 3, 1, decV1Result);

    invoke("/v2/add", 2, 1, addV2Result);
    invoke("/v2/add", 3, 1, addV2Result);

    invoke("/v2/dec", 2, 1, decV2Result);
    invoke("/v2/dec", 3, 1, decV2Result);

    printResults("v1/add", addV1Result);
    printResults("v1/dec", decV1Result);
    printResults("v2/add", addV2Result);
    printResults("v2/dec", decV2Result);

    checkResult("v1/add", addV1Result, "1.0.0", "1.1.0");
    checkResult("v1/dec", decV1Result, "1.1.0");
    checkResult("v2/add", addV2Result, "2.0.0");
    checkResult("v2/dec", decV2Result, "2.0.0");
  }

  public void testEncrypt() {
    prepareEdge("encryptApi");
    String url = edgePrefix + "/v2/encrypt";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("name", "userName");
    form.add("age", "10");
    form.add("serviceToken", "serviceTokenTest");
    form.add("hcrId", "hcrIdTest");
    form.add("body", "bodyKey-hcrIdTest-{\"body1\":\"b1\",\"body2\":\"b2\",\"body3\":\"b3\"}");

    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) template.postForObject(url, entity, Map.class);
    Assert.isTrue(result.containsKey("signature"), "must exist signature");
    result.remove("signature");

    String expected = "{name=userName, age=10, userId=serviceTokenTest-userId, body1=b1, body2=b2, body3=b3}";
    Assert.isTrue(expected.equalsIgnoreCase(result.toString()),
        String.format("expected: %s\nreal    : %s", expected, result.toString()));
  }

  protected void testRecursiveSelf() {
    String url = edgePrefix + "/v2/recursiveSelf";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    RecursiveSelfType recursiveSelfType = new RecursiveSelfType();
    recursiveSelfType.setField(new RecursiveSelfType());
    recursiveSelfType.getField().setValue(10);

    HttpEntity<RecursiveSelfType> entity = new HttpEntity<>(recursiveSelfType, headers);

    RecursiveSelfType response = template.postForObject(url, entity, RecursiveSelfType.class);
    Assert.isTrue(response.getValue() == 0, "default must be 0");
    Assert.isTrue(response.getField().getValue() == 10, "must be 10");
    Assert.isNull(response.getField().getField(), "must be null");
  }

  @SuppressWarnings({"rawtypes"})
  protected void testErrorCode() {
    String url = edgePrefix + "/v2/error/add";

    int response = template.getForObject(url + "?x=2&y=3", Integer.class);
    Assert.isTrue(response == 5, "not get 5.");

    Map raw = template.getForObject(url + "?x=99&y=3", Map.class);
    Assert.isTrue(raw.get("message").equals("Cse Internal Server Error"), "x99");

    try {
      template.getForObject(url + "?x=88&y=3", Map.class);
      Assert.isTrue(false, "x88");
    } catch (HttpClientErrorException e) {
      Assert.isTrue(e.getRawStatusCode() == 403, "x88");
      Assert.isTrue(e.getResponseBodyAsString().equals("{\"id\":12,\"message\":\"not allowed id.\"}"), "x88");
    }
    try {
      template.getForObject(url + "?x=77&y=3", Map.class);
      Assert.isTrue(false, "x77");
    } catch (HttpServerErrorException e) {
      Assert.isTrue(e.getRawStatusCode() == 500, "x77");
      Assert.isTrue(e.getResponseBodyAsString().equals("{\"id\":500,\"message\":\"77\",\"state\":\"77\"}"), "x77");
    }
  }

  protected void testDependType() {
    String url = edgePrefix + "/v2/dependType";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    DependTypeA dependTypeA = new DependTypeA();
    dependTypeA.setB(new DependTypeB());
    dependTypeA.getB().setValue(10);

    HttpEntity<DependTypeA> entity = new HttpEntity<>(dependTypeA, headers);

    DependTypeA response = template.postForObject(url, entity, DependTypeA.class);
    Assert.isTrue(response.getB().getValue() == 10, "must be 10");
  }

  protected void testDownloadBigFile() {
    String url = edgePrefix + "/v2/bigFile";
    AtomicInteger size = new AtomicInteger();

    template.execute(url, HttpMethod.GET, req -> {
    }, resp -> {
      byte[] buf = new byte[1 * 1024 * 1024];
      try (InputStream is = resp.getBody()) {
        for (; ; ) {
          int len = is.read(buf);
          if (len == -1) {
            break;
          }

          size.addAndGet(len);
        }
      }
      return null;
    });
    Assert.isTrue(size.get() == 10 * 1024 * 1024, "size is : " + String.valueOf(size.get()) + " not 10 * 1024 * 1024");
    System.out.println("test download bigFile finished");
  }

  protected void testDownload() {
    String url = edgePrefix + "/v2/download";
    String content = template.getForObject(url, String.class);
    Assert.isTrue("download".equals(content), "content is : " + content + " not download");
    System.out.println("test download finished");
  }

  private void checkResult(String name, List<ResultWithInstance> results, String... expectedVersions) {
    Set<String> versions = new HashSet<>();
    Set<String> remained = new HashSet<>(Arrays.asList(expectedVersions));
    for (ResultWithInstance result : results) {
      versions.add(result.getVersion());
      remained.remove(result.getVersion());
    }

    Assert.isTrue(remained.isEmpty(),
        String.format("%s expectedVersions %s, real versions %s.",
            name,
            Arrays.deepToString(expectedVersions),
            versions));
  }

  protected void printResults(String name, List<ResultWithInstance> results) {
    System.out.println(name);
    for (ResultWithInstance result : results) {
      System.out.println(result);
    }
    System.out.println("");
  }

  protected void invoke(String appendUrl, int x, int y, List<ResultWithInstance> results) {
    String url = edgePrefix + appendUrl + String.format("?x=%d&y=%d", x, y);
    ResultWithInstance result = template.getForObject(url, ResultWithInstance.class);
    results.add(result);
  }

  private URIEndpointObject prepareEdge(String prefix) {
    Microservice microservice = RegistryUtils.getMicroservice();
    MicroserviceInstance microserviceInstance = (MicroserviceInstance) RegistryUtils.getServiceRegistry()
        .getAppManager()
        .getOrCreateMicroserviceVersionRule(microservice.getAppId(), "edge", DefinitionConst.VERSION_RULE_ALL)
        .getVersionedCache()
        .mapData()
        .values()
        .stream()
        .findFirst()
        .get();
    URIEndpointObject edgeAddress = new URIEndpointObject(microserviceInstance.getEndpoints().get(0));
    edgePrefix = String.format("http://%s:%d/%s/business", edgeAddress.getHostOrIp(), edgeAddress.getPort(), prefix);
    return edgeAddress;
  }

  protected void invokeBusiness(String urlPrefix, ChannelRequestBase request) {
    String url = urlPrefix + "/channel/news/subscribe";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<ChannelRequestBase> entity = new HttpEntity<>(request, headers);

    ResponseEntity<AppClientDataRsp> response = template.postForEntity(url, entity, AppClientDataRsp.class);
    System.out.println("urlPrefix: " + urlPrefix);
    System.out.println(response.getHeaders());
    System.out.println(response.getBody().toString());
  }
}
