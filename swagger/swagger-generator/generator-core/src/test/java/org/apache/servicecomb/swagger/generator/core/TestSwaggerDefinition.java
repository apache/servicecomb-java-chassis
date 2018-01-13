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

import org.apache.servicecomb.swagger.generator.core.unittest.SwaggerGeneratorForTest;
import org.apache.servicecomb.swagger.generator.pojo.PojoSwaggerGeneratorContext;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.annotations.Contact;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.ExternalDocs;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import io.swagger.models.Swagger;

public class TestSwaggerDefinition {
  SwaggerGeneratorContext context = new PojoSwaggerGeneratorContext();

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
    SwaggerGenerator swaggerGenerator =
        new SwaggerGeneratorForTest(context, SwaggerAnnotation.class);
    swaggerGenerator.generate();

    Swagger swagger = swaggerGenerator.getSwagger();

    Assert.assertEquals(SwaggerAnnotation.class.getName(),
        swagger.getInfo().getVendorExtensions().get(SwaggerConst.EXT_JAVA_INTF));
    Assert.assertEquals("2.0", swagger.getSwagger());
    Assert.assertEquals("/base", swagger.getBasePath());
    Assert.assertEquals("host", swagger.getHost());
    Assert.assertEquals(Arrays.asList("json", "xml"), swagger.getConsumes());
    Assert.assertEquals(Arrays.asList("abc", "123"), swagger.getProduces());

    Assert.assertEquals(1, swagger.getTags().size());
    io.swagger.models.Tag tagA = swagger.getTags().get(0);
    Assert.assertEquals("tagA", tagA.getName());
    Assert.assertEquals("desc of tagA", tagA.getDescription());
    Assert.assertEquals("tagA ext docs", tagA.getExternalDocs().getDescription());
    Assert.assertEquals("url of tagA ext docs", tagA.getExternalDocs().getUrl());
    Assert.assertEquals(1, tagA.getVendorExtensions().size());

    Map<String, Object> tagValue = (Map<String, Object>) tagA.getVendorExtensions().get("x-tagA");
    Assert.assertEquals("value of tagAExt", tagValue.get("x-tagAExt"));

    io.swagger.models.Info info = swagger.getInfo();
    Assert.assertEquals("title of SwaggerAnnotation", info.getTitle());
    Assert.assertEquals("0.1", info.getVersion());
    Assert.assertEquals("termsOfService", info.getTermsOfService());
    Assert.assertEquals("description of info for SwaggerAnnotation", info.getDescription());

    Assert.assertEquals("contact", info.getContact().getName());
    Assert.assertEquals("contact@email.com", info.getContact().getEmail());
    Assert.assertEquals("http://contact", info.getContact().getUrl());

    Assert.assertEquals("license ", info.getLicense().getName());
    Assert.assertEquals("http://license", info.getLicense().getUrl());

    Assert.assertEquals(2, info.getVendorExtensions().size());

    Map<String, Object> infoValue = (Map<String, Object>) info.getVendorExtensions().get("x-info");
    Assert.assertEquals("value of infoExt", infoValue.get("x-infoExt"));

    Assert.assertEquals("SwaggerAnnotation ext docs", swagger.getExternalDocs().getDescription());
    Assert.assertEquals("url of SwaggerAnnotation ext docs", swagger.getExternalDocs().getUrl());
  }

  @Test
  public void testFillDefault() {
    SwaggerGenerator swaggerGenerator =
        new SwaggerGeneratorForTest(context, SwaggerNoAnnotation.class);
    swaggerGenerator.generate();
    swaggerGenerator.correctSwagger();

    Swagger swagger = swaggerGenerator.getSwagger();

    Assert.assertEquals("2.0", swagger.getSwagger());
    Assert.assertEquals("/SwaggerNoAnnotation", swagger.getBasePath());
    Assert.assertEquals("swagger definition for " + SwaggerNoAnnotation.class.getName(),
        swagger.getInfo().getTitle());
  }
}
