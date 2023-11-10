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

package org.apache.servicecomb.springboot.springmvc.server;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import jakarta.ws.rs.core.MediaType;

@RestSchema(schemaId = "UploadDownloadSchema")
@RequestMapping(path = "/up/down")
public class UploadDownloadSchema {
  @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA)
  public String fileUpload(@RequestPart MultipartFile file, @RequestPart(value = "name") String name) throws Exception {
    StringBuilder result = new StringBuilder();
    result.append(name).append(";");
    if (file == null || file.isEmpty()) {
      return result.toString();
    }
    try (InputStream is = file.getInputStream()) {
      result.append(IOUtils.toString(is, StandardCharsets.UTF_8));
    }
    return result.toString();
  }
}
