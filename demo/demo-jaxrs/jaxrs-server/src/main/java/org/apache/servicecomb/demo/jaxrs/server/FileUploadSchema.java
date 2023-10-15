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
package org.apache.servicecomb.demo.jaxrs.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.provider.rest.common.RestSchema;

import jakarta.servlet.http.Part;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Test and illustrate JaxRS uploads.
 *
 * Use @FormParam to annotate a multipart/form-data item.
 */
@RestSchema(schemaId = "FileUploadSchema")
@Path("/fileUpload")
public class FileUploadSchema {
  @Path("/upload1")
  @POST
  @Produces(MediaType.TEXT_PLAIN)
  public String fileUpload1(@FormParam("file1") Part file1, @FormParam("file2") Part file2) throws IOException {
    if (file1 == null || file2 == null) {
      return "null file";
    }
    try (InputStream is1 = file1.getInputStream(); InputStream is2 = file2.getInputStream()) {
      String content1 = IOUtils.toString(is1, StandardCharsets.UTF_8);
      String content2 = IOUtils.toString(is2, StandardCharsets.UTF_8);
      return String.format("%s:%s:%s\n" + "%s:%s:%s",
          file1.getSubmittedFileName(),
          file1.getContentType(),
          content1,
          file2.getSubmittedFileName(),
          file2.getContentType(),
          content2);
    }
  }

  @Path("/upload2")
  @POST
  @Produces(MediaType.TEXT_PLAIN)
  public String fileUpload2(@FormParam("file1") Part file1, @FormParam("message") String message) throws IOException {
    try (InputStream is1 = file1.getInputStream()) {
      String content1 = IOUtils.toString(is1, StandardCharsets.UTF_8);
      return String.format("%s:%s:%s:%s",
          file1.getSubmittedFileName(),
          file1.getContentType(),
          content1,
          message);
    }
  }
}
