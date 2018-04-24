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
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.endpoint.EndpointsCache;
import org.apache.servicecomb.demo.edge.model.AppClientDataRsp;
import org.apache.servicecomb.demo.edge.model.ChannelRequestBase;
import org.apache.servicecomb.demo.edge.model.ResultWithInstance;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
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

  public void run() {
    prepareEdge();

    testDownload();
    testDownloadBigFile();

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

  protected void testDownloadBigFile() {
    String url = edgePrefix + "/v2/bigFile";
    AtomicInteger size = new AtomicInteger();

    template.execute(url, HttpMethod.GET, req -> {
    }, resp -> {
      byte[] buf = new byte[1 * 1024 * 1024];
      try (InputStream is = resp.getBody()) {
        for (;;) {
          int len = is.read(buf);
          if (len == -1) {
            break;
          }

          size.addAndGet(len);
        }
      }
      return null;
    });
    Assert.isTrue(size.get() == 10 * 1024 * 1024);
    System.out.println("test download bigFile finished");
  }

  protected void testDownload() {
    String url = edgePrefix + "/v2/download";
    String content = template.getForObject(url, String.class);
    Assert.isTrue("download".equals(content));
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

  private URIEndpointObject prepareEdge() {
    Microservice microservice = RegistryUtils.getMicroservice();
    EndpointsCache endpointsCache = new EndpointsCache(microservice.getAppId(), "edge", "latest", "");
    Endpoint ep = endpointsCache.getLatestEndpoints().get(0);
    URIEndpointObject edgeAddress = (URIEndpointObject) ep.getAddress();
    edgePrefix = String.format("http://%s:%d/api/business", edgeAddress.getHostOrIp(), edgeAddress.getPort());
    return edgeAddress;
  }

  protected void invokeBusiness(String urlPrefix, ChannelRequestBase request) {
    String url = urlPrefix + "/channel/news/subscribe";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<ChannelRequestBase> entity = new HttpEntity<ChannelRequestBase>(request, headers);

    ResponseEntity<AppClientDataRsp> response = template.postForEntity(url, entity, AppClientDataRsp.class);
    System.out.println("urlPrefix: " + urlPrefix);
    System.out.println(response.getHeaders());
    System.out.println(response.getBody().toString());
  }
}
