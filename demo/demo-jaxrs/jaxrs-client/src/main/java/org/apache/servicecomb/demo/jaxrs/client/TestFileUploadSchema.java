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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.ws.rs.core.MediaType;

@Component
public class TestFileUploadSchema implements CategorizedTestCase {
  @Override
  public void testRestTransport() throws Exception {
    testUpload(RestTemplateBuilder.create(), "servicecomb://jaxrs/fileUpload");
  }

  private void testUpload(RestTemplate template, String cseUrlPrefix) throws IOException {
    String file1Content = "Hello World";
    File file1 = File.createTempFile("jaxrstest1", ".txt");
    FileUtils.writeStringToFile(file1, file1Content, StandardCharsets.UTF_8, false);

    testFileUpload(template, cseUrlPrefix, file1, file1Content);
    testFileAndStringUpload(template, cseUrlPrefix, file1, file1Content);
  }

  private void testFileUpload(RestTemplate template, String cseUrlPrefix, File file1, String file1Content)
      throws IOException {
    Map<String, Object> map = new HashMap<>();
    map.put("file1", new FileSystemResource(file1));
    String file2Content = "Hello EveryOne";
    File file2 = File.createTempFile("测试2", ".txt");
    FileUtils.writeStringToFile(file2, file2Content, StandardCharsets.UTF_8, false);
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
}
