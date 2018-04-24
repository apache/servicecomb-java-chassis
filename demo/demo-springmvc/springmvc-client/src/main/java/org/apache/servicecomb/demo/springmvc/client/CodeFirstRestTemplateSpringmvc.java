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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.bizkeeper.BizkeeperExceptionUtils;
import org.apache.servicecomb.core.exception.CseException;
import org.apache.servicecomb.demo.CodeFirstRestTemplate;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.foundation.common.part.FilePart;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.provider.springmvc.reference.CseHttpEntity;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class CodeFirstRestTemplateSpringmvc extends CodeFirstRestTemplate {
  interface UploadPartAndFile {
    String fileUpload(Part file1, File file2);
  }
  interface UploadStreamAndResource {
    String fileUpload(InputStream file1, Resource file2);
  }

  private UploadPartAndFile uploadPartAndFile = Invoker.createProxy("springmvc", "codeFirst", UploadPartAndFile.class);

  private UploadStreamAndResource uploadStreamAndResource =
      Invoker.createProxy("springmvc", "codeFirst", UploadStreamAndResource.class);

  private TestResponse testResponse = new TestResponse();

  private TestObject testObject = new TestObject();

  private TestGeneric testGeneric = new TestGeneric();

  private TestDownload testDownload = new TestDownload();

  @Override
  protected void testOnlyRest(RestTemplate template, String cseUrlPrefix) {
    testDownload.runRest();

    try {
      testUpload(template, cseUrlPrefix);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    testResponse.runRest();
    testObject.runRest();
    testGeneric.runRest();

    super.testOnlyRest(template, cseUrlPrefix);
  }

  @Override
  protected void testOnlyHighway(RestTemplate template, String cseUrlPrefix) {
    testResponse.runHighway();
    testObject.runHighway();
    testGeneric.runHighway();

    super.testOnlyHighway(template, cseUrlPrefix);
  }

  @Override
  protected void testAllTransport(String microserviceName, RestTemplate template, String cseUrlPrefix) {
    testResponse.runAllTransport();
    testObject.runAllTransport();
    testGeneric.runAllTransport();

    testResponseEntity("springmvc", template, cseUrlPrefix);
    testCodeFirstTestForm(template, cseUrlPrefix);
    testFallback(template, cseUrlPrefix);

    super.testAllTransport(microserviceName, template, cseUrlPrefix);
  }

  private void testUpload(RestTemplate template, String cseUrlPrefix) throws IOException {
    String file1Content = "hello world";
    File file1 = File.createTempFile("测 试", ".txt");
    FileUtils.writeStringToFile(file1, file1Content);

    String file2Content = " bonjour";
    File someFile = File.createTempFile("upload2", ".txt");
    FileUtils.writeStringToFile(someFile, file2Content);

    String expect = String.format("%s:%s:%s\n"
        + "%s:%s:%s",
        file1.getName(),
        MediaType.TEXT_PLAIN_VALUE,
        file1Content,
        someFile.getName(),
        MediaType.TEXT_PLAIN_VALUE,
        file2Content);

    String result = testRestTemplateUpload(template, cseUrlPrefix, file1, someFile);
    TestMgr.check(expect, result);

    {
      MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add("file1", new FileSystemResource(file1));

      result = template.postForObject(
          cseUrlPrefix + "/upload1",
          new HttpEntity<>(map),
          String.class);

      System.out.println(result);
    }

    result = uploadPartAndFile.fileUpload(new FilePart(null, file1), someFile);
    TestMgr.check(expect, result);

    expect = String.format("null:%s:%s\n"
        + "%s:%s:%s",
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        file1Content,
        someFile.getName(),
        MediaType.TEXT_PLAIN_VALUE,
        file2Content);
    result = uploadStreamAndResource
        .fileUpload(new ByteArrayInputStream(file1Content.getBytes(StandardCharsets.UTF_8)),
            new PathResource(someFile.getAbsolutePath()));
    TestMgr.check(expect, result);
  }

  private String testRestTemplateUpload(RestTemplate template, String cseUrlPrefix, File file1, File someFile) {
    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file1", new FileSystemResource(file1));
    map.add("someFile", new FileSystemResource(someFile));

    return template.postForObject(
        cseUrlPrefix + "/upload",
        new HttpEntity<>(map),
        String.class);
  }

  private void testFallback(RestTemplate template, String cseUrlPrefix) {
    String result = template.getForObject(cseUrlPrefix + "/fallback/returnnull/hello", String.class);
    TestMgr.check(result, "hello");
    result = template.getForObject(cseUrlPrefix + "/fallback/returnnull/throwexception", String.class);
    TestMgr.check(result, null);

    result = template.getForObject(cseUrlPrefix + "/fallback/throwexception/hello", String.class);
    TestMgr.check(result, "hello");
    try {
      result = template.getForObject(cseUrlPrefix + "/fallback/throwexception/throwexception", String.class);
      TestMgr.check(false, true);
    } catch (Exception e) {
      TestMgr.check(((CseException) e.getCause()).getMessage(),
          BizkeeperExceptionUtils.createBizkeeperException(BizkeeperExceptionUtils.CSE_HANDLER_BK_FALLBACK,
              null,
              "springmvc.codeFirst.fallbackThrowException").getMessage());
    }

    result = template.getForObject(cseUrlPrefix + "/fallback/fromcache/hello", String.class);
    TestMgr.check(result, "hello");
    result = template.getForObject(cseUrlPrefix + "/fallback/fromcache/hello", String.class);
    TestMgr.check(result, "hello");
    result = template.getForObject(cseUrlPrefix + "/fallback/fromcache/throwexception", String.class);
    TestMgr.check(result, "hello");

    result = template.getForObject(cseUrlPrefix + "/fallback/force/hello", String.class);
    TestMgr.check(result, "mockedreslut");
  }

  private void testResponseEntity(String microserviceName, RestTemplate template, String cseUrlPrefix) {
    Map<String, Object> body = new HashMap<>();
    Date date = new Date();
    body.put("date", date);

    CseHttpEntity<Map<String, Object>> httpEntity = new CseHttpEntity<>(body);
    httpEntity.addContext("contextKey", "contextValue");

    String srcName = RegistryUtils.getMicroservice().getServiceName();

    ResponseEntity<Date> responseEntity =
        template.exchange(cseUrlPrefix + "responseEntity", HttpMethod.POST, httpEntity, Date.class);
    TestMgr.check(date, responseEntity.getBody());
    TestMgr.check("h1v " + srcName, responseEntity.getHeaders().getFirst("h1"));
    TestMgr.check("h2v " + srcName, responseEntity.getHeaders().getFirst("h2"));
    checkStatusCode(microserviceName, 202, responseEntity.getStatusCode());

    responseEntity =
        template.exchange(cseUrlPrefix + "responseEntity", HttpMethod.PATCH, httpEntity, Date.class);
    TestMgr.check(date, responseEntity.getBody());
    TestMgr.check("h1v " + srcName, responseEntity.getHeaders().getFirst("h1"));
    TestMgr.check("h2v " + srcName, responseEntity.getHeaders().getFirst("h2"));
    checkStatusCode(microserviceName, 202, responseEntity.getStatusCode());
  }

  protected void testCodeFirstTestForm(RestTemplate template, String cseUrlPrefix) {
    HttpHeaders formHeaders = new HttpHeaders();
    formHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    Map<String, String> map = new HashMap<>();
    String code = "servicecomb%2bwelcome%40%23%24%25%5e%26*()%3d%3d";
    map.put("form1", code);
    HttpEntity<Map<String, String>> formEntiry = new HttpEntity<>(map, formHeaders);
    TestMgr.check(code + "null",
        template.postForEntity(cseUrlPrefix + "/testform", formEntiry, String.class).getBody());
    map.put("form2", "");
    TestMgr.check(code + "", template.postForEntity(cseUrlPrefix + "/testform", formEntiry, String.class).getBody());
  }
}
