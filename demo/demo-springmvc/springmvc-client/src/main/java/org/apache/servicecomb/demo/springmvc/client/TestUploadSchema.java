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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class TestUploadSchema implements CategorizedTestCase {
  @Override
  public void testRestTransport() throws Exception {
    testServerStartupSuccess();
    testUploadMultiBigFiles();
  }

  private void testServerStartupSuccess() {
    RestTemplate template = RestTemplateBuilder.create();
    boolean result = template.getForObject("servicecomb://springmvc/upload/isServerStartUpSuccess", Boolean.class);
    TestMgr.check(result, true);
  }

  private void testUploadMultiBigFiles() throws Exception {
    final int fileNum = 5;
    List<File> files = new ArrayList<>(fileNum);

    String fileName = UUID.randomUUID().toString();
    for (int i = 0; i < fileNum; i++) {
      File tempFile = new File("random-client-" + fileName + i);
      files.add(tempFile);
      FileOutputStream fo = new FileOutputStream(tempFile);
      byte[] data = new byte[1024 * 1024 * 10];
      Arrays.fill(data, (byte) 33);
      IOUtils.write(data, fo);
      fo.close();
    }

    RestTemplate template = RestTemplateBuilder.create();

    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    for (int i = 0; i < fileNum; i++) {
      map.add("files", new FileSystemResource(files.get(i)));
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
    HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);

    String result = template.postForObject("servicecomb://springmvc/upload/fileUpload", entity, String.class);
    TestMgr.check(result, "success");

    files.forEach(file -> file.delete());
  }
}
