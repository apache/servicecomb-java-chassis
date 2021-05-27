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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.invocation.endpoint.EndpointUtils;
import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.assertj.core.api.Condition;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import io.netty.channel.ConnectTimeoutException;

public class TestUpload {

  private FileSystemResource fileSystemResource1;

  private FileSystemResource fileSystemResource2;

  private FileSystemResource fileSystemResource3;

  private FileSystemResource fileSystemResource4;

  private static final String message = "cseMessage";

  interface UploadIntf {
    Map<String, String> uploadMultiformMix(Endpoint endpoint, Resource file,
        List<Resource> fileList,
        String str,
        List<String> strList);

    default Map<String, String> uploadMultiformMix(Resource file,
        List<Resource> fileList,
        String str,
        List<String> strList) {
      return uploadMultiformMix(null, file, fileList, str, strList);
    }
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
      FileUtils.writeStringToFile(file1, "hello1", StandardCharsets.UTF_8, false);
      FileUtils.writeStringToFile(file2, "中文 2", StandardCharsets.UTF_8, false);
      FileUtils.writeStringToFile(file3, "cse3", StandardCharsets.UTF_8, false);
      FileUtils.writeStringToFile(file4, "cse4", StandardCharsets.UTF_8, false);
      fileSystemResource1 = new FileSystemResource(file1);
      fileSystemResource2 = new FileSystemResource(file2);
      fileSystemResource3 = new FileSystemResource(file3);
      fileSystemResource4 = new FileSystemResource(file4);
    } catch (IOException e) {
      Assert.fail("Failed to create temp file");
    }
  }

  @Test
  public void should_failed_when_connect_failed() {
    Throwable throwable = catchThrowable(
        () -> consumersJaxrs.getIntf()
            .uploadMultiformMix(EndpointUtils.parse(EndpointUtils.formatFromUri("http://149.159.169.179:54321")),
                fileSystemResource1, singletonList(fileSystemResource2), message, singletonList("2.中文测试")));

    assertThat(throwable)
        .isInstanceOf(InvocationException.class);
    assertThat(throwable.toString())
        .is(anyOf(
            new Condition<>(v -> v.equals(
                "InvocationException: code=500;msg=CommonExceptionData{code='SCB.00000000', message='connection timed out.', dynamic={}}"),
                "for filter"),
            new Condition<>(v -> v.equals(
                "InvocationException: code=490;msg=CommonExceptionData [message=Unexpected consumer error, please check logs for details]"),
                "for handler")
        ));
    assertThat(throwable.getCause())
        .isInstanceOf(ConnectTimeoutException.class)
        .hasMessage("connection timed out: /149.159.169.179:54321");
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

//  @Test
//  public void testJarxUploadArrayList1() {
//    Map<String, Object> map = new HashMap<>();
//    ArrayList<FileSystemResource> list = new ArrayList<>();
//    list.add(fileSystemResource1);
//    list.add(fileSystemResource2);
//    map.put("file1", list);
//    map.put("file2", fileSystemResource3);
//    String result = consumersJaxrs.getSCBRestTemplate()
//        .postForObject("/uploadArrayList1", new HttpEntity<>(map), String.class);
//    Assert.assertTrue(containsAll(result, "hello1", "中文 2", "cse3"));
//  }

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

//  @Test
//  public void testJarxUploadArrayList2() {
//    Map<String, Object> map = new HashMap<>();
//    ArrayList<FileSystemResource> list = new ArrayList<>();
//    list.add(fileSystemResource2);
//    list.add(fileSystemResource3);
//    map.put("file1", list);
//    map.put("message", message);
//    String result = consumersJaxrs.getSCBRestTemplate()
//        .postForObject("/uploadArrayList2", new HttpEntity<>(map), String.class);
//    Assert.assertTrue(containsAll(result, "cse3", "中文 2", message));
//  }

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

//  @Test
//  public void testFileUploadArrayList() {
//    ITJUnitUtils.initForDebug("it-producer", "rest");
//
//    Map<String, Object> map = new HashMap<>();
//    ArrayList<FileSystemResource> list1 = new ArrayList<>();
//    ArrayList<FileSystemResource> list2 = new ArrayList<>();
//    list1.add(fileSystemResource1);
//    list1.add(fileSystemResource2);
//    list2.add(fileSystemResource3);
//    list2.add(fileSystemResource4);
//    map.put("file1", list1);
//    map.put("file2", list2);
//    map.put("name", message);
//    HttpHeaders headers = new HttpHeaders();
//    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//    String result = consumersSpringmvc.getSCBRestTemplate()
//        .postForObject("/uploadArrayList", new HttpEntity<>(map, headers), String.class);
//    Assert.assertTrue(containsAll(result, "hello1", "cse4", "cse3", "中文 2", message));
//  }

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

  @Test
  public void testUploadMultiformMix_RestTemplate_SpringMVC() {
    Map<String, Object> map = new HashMap<>();
    List<Resource> fileList = new ArrayList<>();
    fileList.add(fileSystemResource2);
    map.put("file", fileSystemResource1);
    map.put("fileList", fileList);
    map.put("str", message);
    map.put("strList", singletonList("2.中文测试"));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    ResponseEntity<Map<String, String>> response =
        consumersSpringmvc.getSCBRestTemplate().exchange("/uploadMultiformMix", HttpMethod.POST,
            new HttpEntity<>(map, headers), new ParameterizedTypeReference<Map<String, String>>() {
            });
    Map<String, String> responseBody = response.getBody();
    Assert.assertThat(responseBody, Matchers.notNullValue());
    Assert.assertThat(responseBody.get("file"), Matchers.is("hello1"));
    Assert.assertThat(responseBody.get("fileList"), Matchers.is("中文 2"));
    Assert.assertThat(responseBody.get("str"), Matchers.is("cseMessage"));
    Assert.assertThat(responseBody.get("strList"), Matchers.is("[2.中文测试]"));
  }

  @Test
  public void testUploadMultiformMix_Rpc_SpringMVC() {
    List<Resource> fileList = new ArrayList<>();
    fileList.add(fileSystemResource2);
    Map<String, String> responseBody =
        consumersSpringmvc.getIntf().uploadMultiformMix(
            fileSystemResource1, fileList, message, singletonList("2.中文测试"));
    Assert.assertThat(responseBody.get("file"), Matchers.is("hello1"));
    Assert.assertThat(responseBody.get("fileList"), Matchers.is("中文 2"));
    Assert.assertThat(responseBody.get("str"), Matchers.is("cseMessage"));
    Assert.assertThat(responseBody.get("strList"), Matchers.is("[2.中文测试]"));
  }

  @Test
  public void testUploadMultiformMix_RestTemplate_JAXRS() {
    Map<String, Object> map = new HashMap<>();
    List<FileSystemResource> fileList = new ArrayList<>();
    fileList.add(fileSystemResource2);
    map.put("file", fileSystemResource1);
    map.put("fileList", fileList);
    map.put("str", message);
    map.put("strList", singletonList("2.中文测试"));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    ResponseEntity<Map<String, String>> response =
        consumersJaxrs.getSCBRestTemplate().exchange("/uploadMultiformMix", HttpMethod.POST,
            new HttpEntity<>(map, headers), new ParameterizedTypeReference<Map<String, String>>() {
            });
    Map<String, String> responseBody = response.getBody();
    Assert.assertThat(responseBody, Matchers.notNullValue());
    Assert.assertThat(responseBody.get("file"), Matchers.is("hello1"));
    Assert.assertThat(responseBody.get("fileList"), Matchers.is("中文 2"));
    Assert.assertThat(responseBody.get("str"), Matchers.is("cseMessage"));
    Assert.assertThat(responseBody.get("strList"), Matchers.is("[2.中文测试]"));
  }

  @Test
  public void testUploadMultiformMix_Rpc_JAXRS() {
    List<Resource> fileList = new ArrayList<>();
    fileList.add(fileSystemResource2);
    Map<String, String> responseBody =
        consumersJaxrs.getIntf().uploadMultiformMix(
            fileSystemResource1, fileList, message, singletonList("2.中文测试"));
    Assert.assertThat(responseBody.get("file"), Matchers.is("hello1"));
    Assert.assertThat(responseBody.get("fileList"), Matchers.is("中文 2"));
    Assert.assertThat(responseBody.get("str"), Matchers.is("cseMessage"));
    Assert.assertThat(responseBody.get("strList"), Matchers.is("[2.中文测试]"));
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
