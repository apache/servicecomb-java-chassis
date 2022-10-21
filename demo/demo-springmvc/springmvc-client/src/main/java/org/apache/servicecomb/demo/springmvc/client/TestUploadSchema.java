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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class TestUploadSchema implements CategorizedTestCase {

  interface FileUploadMultiInf {
    String fileUploadMultiRpc(List<Resource> files);
  }

  @RpcReference(microserviceName = "springmvc", schemaId = "UploadSchema")
  private FileUploadMultiInf fileUploadMultiInf;

  @Override
  public void testRestTransport() throws Exception {
    testServerStartupSuccess();
    testUploadMultiBigFiles();
    testFileUploadMultiRpc();
    testUploadFileAndAttribute();
    testUploadFileRequestPartAttribute();
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

    files.forEach(File::delete);
  }

  private void testFileUploadMultiRpc() throws IOException {
    File file1 = File.createTempFile("file1", ".txt");
    File file2 = File.createTempFile("file2", ".txt");
    List<Resource> files = new ArrayList<>();
    files.add(new FileSystemResource(file1));
    files.add(new FileSystemResource(file2));
    String result = fileUploadMultiInf.fileUploadMultiRpc(files);
    TestMgr.check(result, "fileUploadMulti success, and fileNum is 2");
  }

  private void testUploadFileAndAttribute() throws Exception {
    RestTemplate template = RestTemplateBuilder.create();
    Map<String, Object> map = new HashMap<>();
    String message = "hi";
    File file = File.createTempFile("file", ".txt");
    FileUtils.writeStringToFile(file, "test", StandardCharsets.UTF_8, false);

    map.put("file", new FileSystemResource(file));
    map.put("attribute", message);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
    String result = template.postForObject("servicecomb://springmvc/upload/uploadFileAndAttribute",
        new HttpEntity<>(map, headers), String.class);
    TestMgr.check("hi test", result);
  }

  private void testUploadFileRequestPartAttribute() throws Exception {
    RestTemplate template = RestTemplateBuilder.create();
    Map<String, Object> map = new HashMap<>();
    String message = "hi";
    File file = File.createTempFile("file", ".txt");
    FileUtils.writeStringToFile(file, "test", StandardCharsets.UTF_8, false);

    map.put("file", new FileSystemResource(file));
    map.put("attribute", message);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
    String result = template.postForObject("servicecomb://springmvc/upload/uploadFileRequestPartAttribute",
        new HttpEntity<>(map, headers), String.class);
    TestMgr.check("hi test", result);
  }
}
