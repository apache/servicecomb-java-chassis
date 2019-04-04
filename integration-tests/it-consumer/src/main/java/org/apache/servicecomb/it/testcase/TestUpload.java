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
package org.apache.servicecomb.it.testcase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.it.Consumers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class TestUpload {

  private FileSystemResource fileSystemResource1;

  private FileSystemResource fileSystemResource2;

  private FileSystemResource fileSystemResource3;

  private FileSystemResource fileSystemResource4;

  private static final String message = "cseMessage";

  interface UploadIntf {
  }

  private static Consumers<UploadIntf> consumersSpringmvc = new Consumers<>("uploadSpringmvcSchema",
      UploadIntf.class);

  private static Consumers<UploadIntf> consumersJaxrs = new Consumers<>("uploadJaxrsSchema",
      UploadIntf.class);

  @Before
  public void init() {
    try {
      File file1 = File.createTempFile("jaxrstest1", ".txt");
      File file2 = File.createTempFile("测试啊", ".txt");
      File file3 = File.createTempFile("files", ".yaml");
      File file4 = File.createTempFile("files4", ".yaml");
      FileUtils.writeStringToFile(file1, "hello1", Charset.defaultCharset(), false);
      FileUtils.writeStringToFile(file2, "中文 2", Charset.defaultCharset(), false);
      FileUtils.writeStringToFile(file3, "cse3", Charset.defaultCharset(), false);
      FileUtils.writeStringToFile(file4, "cse4", Charset.defaultCharset(), false);
      fileSystemResource1 = new FileSystemResource(file1);
      fileSystemResource2 = new FileSystemResource(file2);
      fileSystemResource3 = new FileSystemResource(file3);
      fileSystemResource4 = new FileSystemResource(file4);
    } catch (IOException e) {
      Assert.fail("Failed to create temp file");
    }
  }

  @Test
  public void testJarxUpload1() {
    Map<String, Object> map = new HashMap<>();
    map.put("file1", fileSystemResource1);
    map.put("file2", fileSystemResource2);
    String result = consumersJaxrs.getSCBRestTemplate().postForObject("/upload1", new HttpEntity<>(map), String.class);
    Assert.assertTrue(containsAll(result, "hello1", "中文 2"));
  }

  @Test
  public void testJarxUploadArray1() {
    Map<String, Object> map = new HashMap<>();
    FileSystemResource[] arrays = {fileSystemResource1, fileSystemResource2};
    map.put("file1", arrays);
    map.put("file2", fileSystemResource3);
    String result = consumersJaxrs.getSCBRestTemplate()
        .postForObject("/uploadArray1", new HttpEntity<>(map), String.class);
    Assert.assertTrue(containsAll(result, "hello1", "中文 2", "cse3"));
  }

  @Test
  public void testJarxUploadList1() {
    Map<String, Object> map = new HashMap<>();
    List<FileSystemResource> list = new ArrayList<>();
    list.add(fileSystemResource1);
    list.add(fileSystemResource2);
    map.put("file1", list);
    map.put("file2", fileSystemResource3);
    String result = consumersJaxrs.getSCBRestTemplate()
        .postForObject("/uploadList1", new HttpEntity<>(map), String.class);
    Assert.assertTrue(containsAll(result, "hello1", "中文 2", "cse3"));
  }

  @Test
  public void testJarxUploadArrayList1() {
    Map<String, Object> map = new HashMap<>();
    ArrayList<FileSystemResource> list = new ArrayList<>();
    list.add(fileSystemResource1);
    list.add(fileSystemResource2);
    map.put("file1", list);
    map.put("file2", fileSystemResource3);
    String result = consumersJaxrs.getSCBRestTemplate()
        .postForObject("/uploadArrayList1", new HttpEntity<>(map), String.class);
    Assert.assertTrue(containsAll(result, "hello1", "中文 2", "cse3"));
  }

  @Test
  public void testJarxUpload2() {
    Map<String, Object> map = new HashMap<>();
    map.put("file1", fileSystemResource1);
    map.put("message", message);
    String result = consumersJaxrs.getSCBRestTemplate()
        .postForObject("/upload2", new HttpEntity<>(map), String.class);
    Assert.assertTrue(containsAll(result, "hello1", message));
  }

  @Test
  public void testJarxUploadArray2() {
    Map<String, Object> map = new HashMap<>();
    FileSystemResource[] arrays = {fileSystemResource1, fileSystemResource2};
    map.put("file1", arrays);
    map.put("message", message);
    String result = consumersJaxrs.getSCBRestTemplate()
        .postForObject("/uploadArray2", new HttpEntity<>(map), String.class);
    Assert.assertTrue(containsAll(result, "hello1", "中文 2", message));
  }

  @Test
  public void testJarxUploadList2() {
    Map<String, Object> map = new HashMap<>();
    List<FileSystemResource> list = new ArrayList<>();
    list.add(fileSystemResource2);
    list.add(fileSystemResource3);
    map.put("file1", list);
    map.put("message", message);
    String result = consumersJaxrs.getSCBRestTemplate()
        .postForObject("/uploadList2", new HttpEntity<>(map), String.class);
    Assert.assertTrue(containsAll(result, "cse3", "中文 2", message));
  }

  @Test
  public void testJarxUploadArrayList2() {
    Map<String, Object> map = new HashMap<>();
    ArrayList<FileSystemResource> list = new ArrayList<>();
    list.add(fileSystemResource2);
    list.add(fileSystemResource3);
    map.put("file1", list);
    map.put("message", message);
    String result = consumersJaxrs.getSCBRestTemplate()
        .postForObject("/uploadArrayList2", new HttpEntity<>(map), String.class);
    Assert.assertTrue(containsAll(result, "cse3", "中文 2", message));
  }

  @Test
  public void testJarxUploadMix() {
    Map<String, Object> map = new HashMap<>();
    List<FileSystemResource> list = new ArrayList<>();
    list.add(fileSystemResource4);
    list.add(fileSystemResource3);
    FileSystemResource[] arrays = {fileSystemResource1, fileSystemResource2};
    map.put("file1", list);
    map.put("file2", arrays);
    map.put("message", message);
    String result = consumersJaxrs.getSCBRestTemplate()
        .postForObject("/uploadMix", new HttpEntity<>(map), String.class);
    Assert.assertTrue(containsAll(result, "hello1", "cse4", "cse3", "中文 2", message));
  }

  //springmvc
  @Test
  public void testFileUpload() {
    Map<String, Object> map = new HashMap<>();
    map.put("file1", fileSystemResource1);
    map.put("file2", fileSystemResource2);
    map.put("name", message);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String result = consumersSpringmvc.getSCBRestTemplate().postForObject("/upload",
        new HttpEntity<>(map, headers), String.class);
    Assert.assertTrue(containsAll(result, "hello1", "中文 2", message));
  }

  @Test
  public void testFileUploadArray() {
    Map<String, Object> map = new HashMap<>();
    FileSystemResource[] array1 = {fileSystemResource1, fileSystemResource2};
    FileSystemResource[] array2 = {fileSystemResource3, fileSystemResource4};
    map.put("file1", array1);
    map.put("file2", array2);
    map.put("name", message);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String result = consumersSpringmvc.getSCBRestTemplate()
        .postForObject("/uploadArray", new HttpEntity<>(map, headers), String.class);
    Assert.assertTrue(containsAll(result, "hello1", "cse4", "cse3", "中文 2", message));
  }

  @Test
  public void testFileUploadList() {
    Map<String, Object> map = new HashMap<>();
    List<FileSystemResource> list1 = new ArrayList<>();
    List<FileSystemResource> list2 = new ArrayList<>();
    list1.add(fileSystemResource1);
    list1.add(fileSystemResource2);
    list2.add(fileSystemResource3);
    list2.add(fileSystemResource4);
    map.put("file1", list1);
    map.put("file2", list2);
    map.put("name", message);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String result = consumersSpringmvc.getSCBRestTemplate()
        .postForObject("/uploadList", new HttpEntity<>(map, headers), String.class);
    Assert.assertTrue(containsAll(result, "hello1", "cse4", "cse3", "中文 2", message));
  }

  @Test
  public void testFileUploadArrayList() {
    Map<String, Object> map = new HashMap<>();
    ArrayList<FileSystemResource> list1 = new ArrayList<>();
    ArrayList<FileSystemResource> list2 = new ArrayList<>();
    list1.add(fileSystemResource1);
    list1.add(fileSystemResource2);
    list2.add(fileSystemResource3);
    list2.add(fileSystemResource4);
    map.put("file1", list1);
    map.put("file2", list2);
    map.put("name", message);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String result = consumersSpringmvc.getSCBRestTemplate()
        .postForObject("/uploadArrayList", new HttpEntity<>(map, headers), String.class);
    Assert.assertTrue(containsAll(result, "hello1", "cse4", "cse3", "中文 2", message));
  }

  @Test
  public void testFileUploadWithoutAnnotation() {
    Map<String, Object> map = new HashMap<>();
    map.put("file1", fileSystemResource1);
    map.put("file2", fileSystemResource2);
    map.put("name", message);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String result = consumersSpringmvc.getSCBRestTemplate().postForObject("/uploadWithoutAnnotation",
        new HttpEntity<>(map, headers), String.class);
    Assert.assertTrue(containsAll(result, "hello1", "中文 2", message));
  }

  @Test
  public void testFileUploadArrayWithoutAnnotation() {
    Map<String, Object> map = new HashMap<>();
    FileSystemResource[] array1 = {fileSystemResource1, fileSystemResource2};
    FileSystemResource[] array2 = {fileSystemResource3, fileSystemResource4};
    map.put("file1", array1);
    map.put("file2", array2);
    map.put("name", message);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String result = consumersSpringmvc.getSCBRestTemplate()
        .postForObject("/uploadArrayWithoutAnnotation", new HttpEntity<>(map, headers), String.class);
    Assert.assertTrue(containsAll(result, "hello1", "cse4", "cse3", "中文 2", message));
  }

  @Test
  public void testFileUploadListWithoutAnnotation() {
    Map<String, Object> map = new HashMap<>();
    List<FileSystemResource> list1 = new ArrayList<>();
    List<FileSystemResource> list2 = new ArrayList<>();
    list1.add(fileSystemResource1);
    list1.add(fileSystemResource2);
    list2.add(fileSystemResource3);
    list2.add(fileSystemResource4);
    map.put("file1", list1);
    map.put("file2", list2);
    map.put("name", message);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String result = consumersSpringmvc.getSCBRestTemplate()
        .postForObject("/uploadListArrayWithoutAnnotation", new HttpEntity<>(map, headers), String.class);
    Assert.assertTrue(containsAll(result, "hello1", "cse4", "cse3", "中文 2", message));
  }

  @Test
  public void testFileUploadArrayListWithoutAnnotation() {
    Map<String, Object> map = new HashMap<>();
    ArrayList<FileSystemResource> list1 = new ArrayList<>();
    ArrayList<FileSystemResource> list2 = new ArrayList<>();
    list1.add(fileSystemResource1);
    list1.add(fileSystemResource2);
    list2.add(fileSystemResource3);
    list2.add(fileSystemResource4);
    map.put("file1", list1);
    map.put("file2", list2);
    map.put("name", message);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String result = consumersSpringmvc.getSCBRestTemplate()
        .postForObject("/uploadArrayListArrayWithoutAnnotation", new HttpEntity<>(map, headers), String.class);
    Assert.assertTrue(containsAll(result, "hello1", "cse4", "cse3", "中文 2", message));
  }

  @Test
  public void testFileUploadMixWithoutAnnotation() {
    Map<String, Object> map = new HashMap<>();
    List<FileSystemResource> list1 = new ArrayList<>();
    list1.add(fileSystemResource1);
    list1.add(fileSystemResource2);
    FileSystemResource[] array2 = {fileSystemResource3, fileSystemResource4};
    map.put("file1", list1);
    map.put("file2", array2);
    map.put("name", message);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String result = consumersSpringmvc.getSCBRestTemplate()
        .postForObject("/uploadMix", new HttpEntity<>(map, headers), String.class);
    Assert.assertTrue(containsAll(result, "hello1", "cse4", "cse3", "中文 2", message));
  }


  private static boolean containsAll(String str, String... strings) {
    for (String string : strings) {
      if (!str.contains(string)) {
        return false;
      }
    }
    return true;
  }
}
