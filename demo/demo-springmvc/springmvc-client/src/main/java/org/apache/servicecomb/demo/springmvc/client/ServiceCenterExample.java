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

package org.apache.servicecomb.demo.springmvc.client;

import java.net.URI;

import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class ServiceCenterExample {

  public static void main(String[] args) throws Exception {
    RestTemplate template = new RestTemplate();
    template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("X-Tenant-Name", "default");

    RequestEntity<String> requestEntity = new RequestEntity<String>(headers, HttpMethod.GET,
        new URI("http://127.0.0.1:9980/registry/v3/microservices"));
    ResponseEntity<String> stringResponseEntity = template.exchange(requestEntity, String.class);
    System.out.println(stringResponseEntity.getBody());
    ResponseEntity<MicroserviceArray> microseriveResponseEntity = template
        .exchange(requestEntity, MicroserviceArray.class);
    MicroserviceArray microserives = microseriveResponseEntity.getBody();
    System.out.println(microserives.getServices().get(1).getServiceId());

    // instance
    headers.add("X-ConsumerId", microserives.getServices().get(1).getServiceId());
    requestEntity = new RequestEntity<String>(headers, HttpMethod.GET,
        new URI("http://127.0.0.1:9980/registry/v3/microservices/" + microserives.getServices().get(1).getServiceId()
            + "/instances"));
    ResponseEntity<String> microserviceInstanceResponseEntity = template.exchange(requestEntity, String.class);
    System.out.println(microserviceInstanceResponseEntity.getBody());
  }
}
