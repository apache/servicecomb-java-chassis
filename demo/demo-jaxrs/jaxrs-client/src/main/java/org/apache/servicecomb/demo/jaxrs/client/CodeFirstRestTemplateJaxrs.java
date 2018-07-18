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

package org.apache.servicecomb.demo.jaxrs.client;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.demo.CodeFirstRestTemplate;
import org.apache.servicecomb.demo.TestMgr;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class CodeFirstRestTemplateJaxrs extends CodeFirstRestTemplate {
  @Override
  protected void testAllTransport(String microserviceName, RestTemplate template, String cseUrlPrefix) {
    testDefaultPath(template, cseUrlPrefix);
    test404(template);

    super.testAllTransport(microserviceName, template, cseUrlPrefix);
  }

  private void testDefaultPath(RestTemplate template, String cseUrlPrefix) {
    int result =
        template.getForObject(cseUrlPrefix.substring(0, cseUrlPrefix.length() - 1), Integer.class);
    TestMgr.check(100, result);
  }

  @Override
  protected void testOnlyRest(RestTemplate template, String cseUrlPrefix) {
    try {
      testUpload(template, cseUrlPrefix);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    super.testOnlyRest(template, cseUrlPrefix);
  }

  private void testUpload(RestTemplate template, String cseUrlPrefix) throws IOException {
    String file1Content = "Hello World";
    File file1 = File.createTempFile("jaxrstest1", ".txt");
    FileUtils.writeStringToFile(file1, file1Content);

    testFileUpload(template, cseUrlPrefix, file1, file1Content);
    testFileAndStringUpload(template, cseUrlPrefix, file1, file1Content);
  }

  private void testFileUpload(RestTemplate template, String cseUrlPrefix, File file1, String file1Content)
      throws IOException {
    Map<String, Object> map = new HashMap<>();
    map.put("file1", new FileSystemResource(file1));
    String file2Content = "Hello EveryOne";
    File file2 = File.createTempFile("测试2", ".txt");
    FileUtils.writeStringToFile(file2, file2Content);
    map.put("file2", new FileSystemResource(file2));

    String result1 = template.postForObject(cseUrlPrefix + "/upload1", new HttpEntity<>(new HashMap<>()), String.class);
    TestMgr.check("null file", result1);

    String expect = String.format("%s:%s:%s\n" + "%s:%s:%s",
        file1.getName(),
        MediaType.TEXT_PLAIN,
        file1Content,
        file2.getName(),
        MediaType.TEXT_PLAIN,
        file2Content);
    String result2 = template.postForObject(cseUrlPrefix + "/upload1", new HttpEntity<>(map), String.class);
    TestMgr.check(expect, result2);
  }

  private void testFileAndStringUpload(RestTemplate template, String cseUrlPrefix, File file1, String file1Content) {
    Map<String, Object> map = new HashMap<>();
    String message = "hi";
    map.put("file1", new FileSystemResource(file1));
    map.put("message", message);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
    String expect = String.format("%s:%s:%s:%s",
        file1.getName(),
        MediaType.TEXT_PLAIN,
        file1Content,
        message);
    String result = template.postForObject(cseUrlPrefix + "/upload2", new HttpEntity<>(map, headers), String.class);
    TestMgr.check(expect, result);
  }

  private void test404(RestTemplate template) {
    HttpClientErrorException exception = null;
    try {
      template.getForEntity("http://127.0.0.1:8080/aPathNotExist", String.class);
    } catch (RestClientException e) {
      if (e instanceof HttpClientErrorException) {
        exception = (HttpClientErrorException) e;
      }
    }
    TestMgr.check(404, exception.getRawStatusCode());
    TestMgr.check("404 Not Found", exception.getMessage());
  }
}
