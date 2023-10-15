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

package org.apache.servicecomb.swagger.generator.core;

import java.util.Map;

import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;

@SuppressWarnings("unchecked")
public class TestSwaggerDefinition {
  @OpenAPIDefinition(
      servers = @Server(url = "host/base"),
      tags = {@Tag(
          name = "tagA",
          description = "desc of tagA",
          externalDocs = @ExternalDocumentation(description = "tagA ext docs", url = "url of tagA ext docs"),
          extensions = {@Extension(
              name = "x-tagA",
              properties = {@ExtensionProperty(name = "x-tagAExt", value = "value of tagAExt")})})},
      info = @Info(
          title = "title of SwaggerAnnotation",
          version = "0.1",
          termsOfService = "termsOfService",
          description = "description of info for SwaggerAnnotation",
          contact = @Contact(name = "contact", email = "contact@email.com", url = "http://contact"),
          license = @License(name = "license ", url = "http://license"),
          extensions = {@Extension(
              name = "x-info",
              properties = {@ExtensionProperty(name = "x-infoExt", value = "value of infoExt")})}),
      externalDocs = @ExternalDocumentation(
          description = "SwaggerAnnotation ext docs",
          url = "url of SwaggerAnnotation ext docs"))
  interface SwaggerAnnotation {
  }

  interface SwaggerNoAnnotation {
  }

  @Test
  public void testSwaggerDefinition() {
    OpenAPI swagger = SwaggerGenerator.generate(SwaggerAnnotation.class);

    Assertions.assertEquals("value of infoExt",
        ((Map<String, String>) swagger.getInfo().getExtensions().get("x-info")).get("x-infoExt"));
    Assertions.assertEquals("3.0.1", swagger.getOpenapi());
    Assertions.assertEquals("host/base", swagger.getServers().get(0).getUrl());

    Assertions.assertEquals(1, swagger.getTags().size());
    io.swagger.v3.oas.models.tags.Tag tagA = swagger.getTags().get(0);
    Assertions.assertEquals("tagA", tagA.getName());
    Assertions.assertEquals("desc of tagA", tagA.getDescription());
    Assertions.assertEquals("tagA ext docs", tagA.getExternalDocs().getDescription());
    Assertions.assertEquals("url of tagA ext docs", tagA.getExternalDocs().getUrl());
    Assertions.assertEquals(1, tagA.getExtensions().size());

    Map<String, String> tagValue = (Map<String, String>) tagA.getExtensions().get("x-tagA");
    Assertions.assertEquals("value of tagAExt", tagValue.get("x-tagAExt"));

    io.swagger.v3.oas.models.info.Info info = swagger.getInfo();
    Assertions.assertEquals("title of SwaggerAnnotation", info.getTitle());
    Assertions.assertEquals("0.1", info.getVersion());
    Assertions.assertEquals("termsOfService", info.getTermsOfService());
    Assertions.assertEquals("description of info for SwaggerAnnotation", info.getDescription());

    Assertions.assertEquals("contact", info.getContact().getName());
    Assertions.assertEquals("contact@email.com", info.getContact().getEmail());
    Assertions.assertEquals("http://contact", info.getContact().getUrl());

    Assertions.assertEquals("license ", info.getLicense().getName());
    Assertions.assertEquals("http://license", info.getLicense().getUrl());

    Assertions.assertEquals("SwaggerAnnotation ext docs", swagger.getExternalDocs().getDescription());
    Assertions.assertEquals("url of SwaggerAnnotation ext docs", swagger.getExternalDocs().getUrl());
  }

  @Test
  public void testFillDefault() {
    OpenAPI swagger = SwaggerGenerator.generate(SwaggerNoAnnotation.class);
    Assertions.assertEquals("3.0.1", swagger.getOpenapi());
    Assertions.assertEquals("/SwaggerNoAnnotation", swagger.getServers().get(0).getUrl());
    Assertions.assertEquals("swagger definition for " + SwaggerNoAnnotation.class.getName(),
        swagger.getInfo().getTitle());
  }
}
