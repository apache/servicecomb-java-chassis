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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.it.Consumers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;

public class TestUpload {

  private File file1;

  private File file2;

  private File file3;

  interface UploadIntf {

  }

  private static Consumers<UploadIntf> consumersSpringmvc = new Consumers<>("uploadSpringmvcSchema",
      UploadIntf.class);

  private static Consumers<UploadIntf> consumersJaxrs = new Consumers<>("uploadJaxrsSchema",
      UploadIntf.class);

  @Before
  public void init(){
    try {
      file1 = File.createTempFile("jaxrstest1", ".txt");
      file2 =  File.createTempFile("测试", ".txt");
      file3 = File.createTempFile("files", ".yaml");
      FileUtils.writeStringToFile(file1, "hello1");
      FileUtils.writeStringToFile(file2, "中文 2");
      FileUtils.writeStringToFile(file2, "cse3");
    } catch (IOException e) {
      Assert.fail("Failed to create temp file");
    }
  }

  @Test
  public void testJarxUpload1() {
    Map<String, Object> map = new HashMap<>();
    map.put("file1", new FileSystemResource(file1));
    map.put("file2", new FileSystemResource(file2));
    String upload1 = consumersJaxrs.getSCBRestTemplate().postForObject("upload1", new HttpEntity<>(map), String.class);
    Assert.assertTrue(constainsAll(upload1, "hello1", "中文 2"));
  }

  private static boolean constainsAll(String str, String... strings) {
    for (String string : strings) {
      if (!str.contains(string)) {
        return false;
      }
    }
    return true;
  }

}
