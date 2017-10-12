/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.demo.edge.consumer;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import io.servicecomb.core.Endpoint;
import io.servicecomb.core.endpoint.EndpointsCache;
import io.servicecomb.demo.edge.model.AppClientDataRsp;
import io.servicecomb.demo.edge.model.ChannelRequestBase;
import io.servicecomb.foundation.common.net.URIEndpointObject;
import io.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;

public class Consumer {
  RestTemplate template = RestTemplateBuilder.create();

  ChannelRequestBase request = new ChannelRequestBase();

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
    Microservice microservice = RegistryUtils.getMicroservice();
    EndpointsCache endpointsCache = new EndpointsCache(microservice.getAppId(), "edge", "latest", "");
    Endpoint ep = endpointsCache.getLatestEndpoints().get(0);
    URIEndpointObject edgeAddress = (URIEndpointObject) ep.getAddress();
    invoke(String.format("http://%s:%d/api/business", edgeAddress.getHostOrIp(), edgeAddress.getPort()), request);
    invoke("cse://business", request);

  }

  private void invoke(String urlPrefix, ChannelRequestBase request) {
    String url = urlPrefix + "/channel/v2/news/subscribe";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<ChannelRequestBase> entity = new HttpEntity<ChannelRequestBase>(request, headers);

    ResponseEntity<AppClientDataRsp> response = template.postForEntity(url, entity, AppClientDataRsp.class);
    System.out.println("urlPrefix: " + urlPrefix);
    System.out.println(response.getHeaders());
    System.out.println(response.getBody().toString());
  }
}
