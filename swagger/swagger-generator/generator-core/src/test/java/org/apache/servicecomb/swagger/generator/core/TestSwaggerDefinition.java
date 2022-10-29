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

import java.util.Arrays;
import java.util.Map;

import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;

import io.swagger.annotations.Contact;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.ExternalDocs;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import io.swagger.models.Swagger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestSwaggerDefinition {
  @SwaggerDefinition(
      basePath = "base",
      host = "host",
      consumes = {"json", "xml"},
      produces = {"abc", "123"},
      tags = {@Tag(
          name = "tagA",
          description = "desc of tagA",
          externalDocs = @ExternalDocs(value = "tagA ext docs", url = "url of tagA ext docs"),
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
      externalDocs = @ExternalDocs(
          value = "SwaggerAnnotation ext docs",
          url = "url of SwaggerAnnotation ext docs"))
  interface SwaggerAnnotation {
  }

  interface SwaggerNoAnnotation {
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSwaggerDefinition() {
    Swagger swagger = SwaggerGenerator.generate(SwaggerAnnotation.class);

    Assertions.assertEquals(SwaggerAnnotation.class.getName(),
        swagger.getInfo().getVendorExtensions().get(SwaggerConst.EXT_JAVA_INTF));
    Assertions.assertEquals("2.0", swagger.getSwagger());
    Assertions.assertEquals("/base", swagger.getBasePath());
    Assertions.assertEquals("host", swagger.getHost());
    Assertions.assertEquals(Arrays.asList("json", "xml"), swagger.getConsumes());
    Assertions.assertEquals(Arrays.asList("abc", "123"), swagger.getProduces());

    Assertions.assertEquals(1, swagger.getTags().size());
    io.swagger.models.Tag tagA = swagger.getTags().get(0);
    Assertions.assertEquals("tagA", tagA.getName());
    Assertions.assertEquals("desc of tagA", tagA.getDescription());
    Assertions.assertEquals("tagA ext docs", tagA.getExternalDocs().getDescription());
    Assertions.assertEquals("url of tagA ext docs", tagA.getExternalDocs().getUrl());
    Assertions.assertEquals(1, tagA.getVendorExtensions().size());

    Map<String, Object> tagValue = (Map<String, Object>) tagA.getVendorExtensions().get("x-tagA");
    Assertions.assertEquals("value of tagAExt", tagValue.get("x-tagAExt"));

    io.swagger.models.Info info = swagger.getInfo();
    Assertions.assertEquals("title of SwaggerAnnotation", info.getTitle());
    Assertions.assertEquals("0.1", info.getVersion());
    Assertions.assertEquals("termsOfService", info.getTermsOfService());
    Assertions.assertEquals("description of info for SwaggerAnnotation", info.getDescription());

    Assertions.assertEquals("contact", info.getContact().getName());
    Assertions.assertEquals("contact@email.com", info.getContact().getEmail());
    Assertions.assertEquals("http://contact", info.getContact().getUrl());

    Assertions.assertEquals("license ", info.getLicense().getName());
    Assertions.assertEquals("http://license", info.getLicense().getUrl());

    Assertions.assertEquals(2, info.getVendorExtensions().size());

    Map<String, Object> infoValue = (Map<String, Object>) info.getVendorExtensions().get("x-info");
    Assertions.assertEquals("value of infoExt", infoValue.get("x-infoExt"));

    Assertions.assertEquals("SwaggerAnnotation ext docs", swagger.getExternalDocs().getDescription());
    Assertions.assertEquals("url of SwaggerAnnotation ext docs", swagger.getExternalDocs().getUrl());
  }

  @Test
  public void testFillDefault() {
    Swagger swagger = SwaggerGenerator.generate(SwaggerNoAnnotation.class);
    Assertions.assertEquals("2.0", swagger.getSwagger());
    Assertions.assertEquals("/SwaggerNoAnnotation", swagger.getBasePath());
    Assertions.assertEquals("swagger definition for " + SwaggerNoAnnotation.class.getName(),
        swagger.getInfo().getTitle());
  }
}
