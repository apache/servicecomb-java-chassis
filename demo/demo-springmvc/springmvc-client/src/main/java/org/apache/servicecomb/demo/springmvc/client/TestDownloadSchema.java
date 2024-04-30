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
import java.util.Collections;
import java.util.List;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.foundation.vertx.http.ReadStreamPart;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Component
public class TestDownloadSchema implements CategorizedTestCase {
  @Autowired
  DiscoveryManager discoveryManager;

  @Override
  public void testRestTransport() throws Exception {
    testDownloadFileAndDeleted();
    testDownloadFileNotDeleted();
    testDownloadFileWithNull();
    testSetContentTypeByResponseEntity();
    testResponseOKException();
  }

  private void testResponseOKException() {
    List<? extends DiscoveryInstance> instances =
        discoveryManager.findServiceInstances("springmvctest", "springmvc");
    String endpoint = instances.get(0).getEndpoints().stream()
        .filter(item -> item.startsWith("rest")).findFirst().get();
    URI endpointItem = URI.create(endpoint);
    RestTemplate template = new RestTemplate();

    // This is for compatible usage. For best practise, any status code
    // should have only one type of response.
    ResponseEntity<ResponseOKData> resultFail = template.getForEntity(
        "http://" + endpointItem.getHost() + ":" + endpointItem.getPort()
            + "/api/download/testResponseOKExceptionBean?exception=true", ResponseOKData.class);
    TestMgr.check(200, resultFail.getStatusCode().value());
    TestMgr.check("code-005", resultFail.getBody().getErrorCode());
    TestMgr.check("error-005", resultFail.getBody().getErrorMessage());
    ResponseEntity<Boolean> resultOK = template.getForEntity(
        "http://" + endpointItem.getHost() + ":" + endpointItem.getPort()
            + "/api/download/testResponseOKExceptionBean?exception=false", boolean.class);
    TestMgr.check(true, resultOK.getBody());

    resultFail = template.getForEntity(
        "http://" + endpointItem.getHost() + ":" + endpointItem.getPort()
            + "/api/download/testResponseOKExceptionDownload?exception=true&content=ddd&contentType=plain/text",
        ResponseOKData.class);
    TestMgr.check(200, resultFail.getStatusCode().value());
    TestMgr.check("code-005", resultFail.getBody().getErrorCode());
    TestMgr.check("error-005", resultFail.getBody().getErrorMessage());

    ResponseEntity<String> resultPartOK = template.getForEntity(
        "http://" + endpointItem.getHost() + ":" + endpointItem.getPort()
            + "/api/download/testResponseOKExceptionDownload?exception=false&content=ddd&contentType=plain/text",
        String.class);
    TestMgr.check(200, resultPartOK.getStatusCode().value());
    TestMgr.check("ddd", resultPartOK.getBody());
  }

  private void testDownloadFileAndDeleted() throws Exception {
    RestOperations restTemplate = RestTemplateBuilder.create();
    ReadStreamPart readStreamPart = restTemplate
        .getForObject("servicecomb://springmvc/download/deleteAfterFinished?content=hello", ReadStreamPart.class);
    String hello = readStreamPart.saveAsString().get();
    TestMgr.check(hello, "hello");

    boolean exists = restTemplate
        .getForObject("servicecomb://springmvc/download/assertLastFileDeleted", boolean.class);
    TestMgr.check(exists, false);
  }

  private void testDownloadFileWithNull() throws Exception {
    RestOperations restTemplate = RestTemplateBuilder.create();
    ReadStreamPart readStreamPart = restTemplate
        .getForObject("servicecomb://springmvc/download/partIsNull?content=test", ReadStreamPart.class);
    String result = readStreamPart.saveAsString().get();
    TestMgr.check(result, "test");

    readStreamPart = restTemplate
        .getForObject("servicecomb://springmvc/download/partIsNull?content=", ReadStreamPart.class);
    result = readStreamPart.saveAsString().get();
    TestMgr.check(result, "");
  }

  private void testDownloadFileNotDeleted() throws Exception {
    RestOperations restTemplate = RestTemplateBuilder.create();
    ReadStreamPart readStreamPart = restTemplate
        .getForObject("servicecomb://springmvc/download/notDeleteAfterFinished?content=hello", ReadStreamPart.class);
    String hello = readStreamPart.saveAsString().get();
    TestMgr.check(hello, "hello");

    boolean exists = restTemplate
        .getForObject("servicecomb://springmvc/download/assertLastFileDeleted", boolean.class);
    TestMgr.check(exists, true);
  }

  private void testSetContentTypeByResponseEntity() throws Exception {
    RestOperations restTemplate = RestTemplateBuilder.create();
    ResponseEntity<ReadStreamPart> responseEntity = restTemplate
        .getForEntity(
            "servicecomb://springmvc/download/setContentTypeByResponseEntity?content=hello&contentType=customType",
            ReadStreamPart.class);
    String hello = responseEntity.getBody().saveAsString().get();
    TestMgr.check(responseEntity.getHeaders().get(HttpHeaders.CONTENT_TYPE), Collections.singletonList("customType"));
    TestMgr.check(hello, "hello");
  }
}
