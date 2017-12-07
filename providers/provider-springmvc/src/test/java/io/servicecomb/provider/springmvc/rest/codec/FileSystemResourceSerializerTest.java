/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.provider.springmvc.rest.codec;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class FileSystemResourceSerializerTest {

  private static final String path = "/tmp/path/to/file";
  private final FileSystemResourceSerializer serializer = new FileSystemResourceSerializer();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void setUp() throws Exception {
    SimpleModule module = new SimpleModule();
    module.addSerializer(FileSystemResource.class, serializer);
    objectMapper.registerModule(module);
  }

  @Test
  public void serializeFilePath() throws Exception {
    String json = objectMapper.writeValueAsString(new FileSystemResource(path));

    assertThat(json, is("\"" + path + "\""));
  }
}