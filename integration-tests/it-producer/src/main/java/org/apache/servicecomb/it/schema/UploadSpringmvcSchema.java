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
package org.apache.servicecomb.it.schema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;

@RestSchema(schemaId = "uploadSpringmvcSchema")
@RequestMapping(path = "/v1/uploadSpringmvcSchema")
public class UploadSpringmvcSchema {

  @RequestMapping(path = "/upload", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String fileUpload(@RequestPart(name = "file1") MultipartFile file1,
      @RequestPart(name = "file2") MultipartFile file2, @RequestAttribute("name") String name) {
    return _fileUpload(Lists.newArrayList(file1, file2)) + name;
  }

  @RequestMapping(path = "/uploadArray", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String fileUploadArray(@RequestPart(name = "file1") MultipartFile[] file1,
      @RequestPart(name = "file2") MultipartFile[] file2, @RequestAttribute("name") String name) {
    List<MultipartFile> multipartFileList = new ArrayList<>();
    multipartFileList.addAll(Arrays.asList(file1));
    multipartFileList.addAll(Arrays.asList(file2));
    return _fileUpload(multipartFileList) + name;
  }

  @RequestMapping(path = "/uploadList", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String fileUploadList(@RequestPart(name = "file1") List<MultipartFile> file1,
      @RequestPart(name = "file2") List<MultipartFile> file2, @RequestAttribute("name") String name) {
    file1.addAll(file2);
    return _fileUpload(file1) + name;
  }

//  @RequestMapping(path = "/uploadArrayList", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//  public String fileUploadArrayList(@RequestPart(name = "file1") ArrayList<MultipartFile> file1,
//      @RequestPart(name = "file2") ArrayList<MultipartFile> file2, @RequestAttribute("name") String name) {
//    file1.addAll(file2);
//    return _fileUpload(file1) + name;
//  }

  @RequestMapping(path = "/uploadWithoutAnnotation", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String uploadWithoutAnnotation(MultipartFile file1, MultipartFile file2,
      @RequestAttribute("name") String name) {
    return _fileUpload(Lists.newArrayList(file1, file2)) + name;
  }

  @RequestMapping(path = "/uploadArrayWithoutAnnotation", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String uploadArrayWithoutAnnotation(MultipartFile[] file1, MultipartFile[] file2,
      @RequestAttribute("name") String name) {
    List<MultipartFile> multipartFileList = new ArrayList<>();
    multipartFileList.addAll(Arrays.asList(file1));
    multipartFileList.addAll(Arrays.asList(file2));
    return _fileUpload(multipartFileList) + name;
  }

  @RequestMapping(path = "/uploadListArrayWithoutAnnotation", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String uploadListWithoutAnnotation(List<MultipartFile> file1, List<MultipartFile> file2,
      @RequestAttribute("name") String name) {
    file1.addAll(file2);
    return _fileUpload(file1) + name;
  }

  @RequestMapping(path = "/uploadMix", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String uploadMix(@RequestPart(name = "file1") List<MultipartFile> file1,
      @RequestPart(name = "file2") MultipartFile[] file2, @RequestAttribute("name") String name) {
    List<MultipartFile> multipartFileList = Arrays.asList(file2);
    file1.addAll(multipartFileList);
    return _fileUpload(file1) + name;
  }

  @RequestMapping(path = "/uploadMultiformMix", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Map<String, String> uploadMultiformMix(@RequestPart(name = "file") MultipartFile file,
      @RequestPart(name = "fileList") List<MultipartFile> fileList,
      @RequestPart("str") String str,
      @RequestPart("strList") List<String> strList) throws IOException {
    HashMap<String, String> map = new HashMap<>();
    map.put("file", new String(file.getBytes(), StandardCharsets.UTF_8.name()));
    map.put("fileList", _fileUpload(fileList));
    map.put("str", str);
    map.put("strList", strList.toString());
    return map;
  }

  private static String _fileUpload(List<MultipartFile> fileList) {
    StringBuilder stringBuilder = new StringBuilder();
    try {
      for (MultipartFile multipartFile : fileList) {
        stringBuilder.append(IOUtils.toString(multipartFile.getBytes(), StandardCharsets.UTF_8.name()));
      }
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    return stringBuilder.toString();
  }
}
