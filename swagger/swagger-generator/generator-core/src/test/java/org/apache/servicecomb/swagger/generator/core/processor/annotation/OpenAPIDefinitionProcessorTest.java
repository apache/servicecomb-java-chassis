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

package org.apache.servicecomb.swagger.generator.core.processor.annotation;

import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;

public class OpenAPIDefinitionProcessorTest {
  @OpenAPIDefinition(tags = {
      @Tag(name = "testTag", description = "desc", externalDocs = @ExternalDocumentation(description = "testValue", url = "testUrl"))
  },
      servers = {@Server(url = "127.0.0.1")},
      info = @Info(title = "title", version = "version", description = "desc", contact = @Contact(name = "contactName"),
          license = @License(name = "licenseName")))
  private static class SwaggerTestTarget {
  }

  @Test
  public void testProcess() {
    OpenAPI swagger = SwaggerGenerator.generate(SwaggerTestTarget.class);

    Assertions.assertEquals(1, swagger.getTags().size());
    io.swagger.v3.oas.models.tags.Tag tag = swagger.getTags().get(0);
    Assertions.assertEquals("testTag", tag.getName());
    Assertions.assertEquals("desc", tag.getDescription());
    Assertions.assertEquals("testValue", tag.getExternalDocs().getDescription());
    Assertions.assertEquals("testUrl", tag.getExternalDocs().getUrl());
    Assertions.assertEquals("127.0.0.1", swagger.getServers().get(0).getUrl());

    io.swagger.v3.oas.models.info.Info info = swagger.getInfo();
    Assertions.assertEquals("title", info.getTitle());
    Assertions.assertEquals("version", info.getVersion());
    Assertions.assertEquals("desc", info.getDescription());
    Assertions.assertEquals("contactName", info.getContact().getName());
    Assertions.assertEquals("licenseName", info.getLicense().getName());
  }
}
