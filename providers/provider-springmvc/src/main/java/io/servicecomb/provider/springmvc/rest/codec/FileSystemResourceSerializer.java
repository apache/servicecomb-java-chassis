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

import java.io.IOException;

import org.springframework.core.io.FileSystemResource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

class FileSystemResourceSerializer extends StdSerializer<FileSystemResource> {
  private static final long serialVersionUID = 5167703179451606699L;

  FileSystemResourceSerializer() {
    super(FileSystemResource.class);
  }

  @Override
  public void serialize(FileSystemResource fileSystemResource, JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeString(fileSystemResource.getPath());
  }
}
