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

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import io.swagger.annotations.Contact;
import io.swagger.annotations.ExternalDocs;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.SwaggerDefinition.Scheme;
import io.swagger.annotations.Tag;
import io.swagger.models.Swagger;

public class SwaggerDefinitionProcessorTest {

  private SwaggerDefinitionProcessor swaggerDefinitionProcessor = new SwaggerDefinitionProcessor();

  @Test
  public void testProcess() {
    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(Mockito.mock(SwaggerGeneratorContext.class),
        null);
    Swagger swagger = swaggerGenerator.getSwagger();
    // to test MediaType overwrite
    swagger.addConsumes(MediaType.APPLICATION_XML);
    swagger.addProduces(MediaType.APPLICATION_XML);
    swaggerDefinitionProcessor.process(SwaggerTestTarget.class.getAnnotation(SwaggerDefinition.class),
        swaggerGenerator);

    assertEquals(1, swagger.getTags().size());
    io.swagger.models.Tag tag = swagger.getTags().get(0);
    assertEquals("testTag", tag.getName());
    assertEquals("desc", tag.getDescription());
    assertEquals("testValue", tag.getExternalDocs().getDescription());
    assertEquals("testUrl", tag.getExternalDocs().getUrl());
    assertEquals("127.0.0.1", swagger.getHost());
    assertThat(swagger.getSchemes(), contains(io.swagger.models.Scheme.HTTP, io.swagger.models.Scheme.HTTPS));
    io.swagger.models.Info info = swagger.getInfo();
    assertEquals("title", info.getTitle());
    assertEquals("version", info.getVersion());
    assertEquals("desc", info.getDescription());
    assertEquals("contactName", info.getContact().getName());
    assertEquals("licenseName", info.getLicense().getName());
    assertThat(swagger.getConsumes(), Matchers.contains(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
    assertThat(swagger.getProduces(), Matchers.contains(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML));
  }

  @Test
  public void testProcess_emptyMediaType() {
    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(Mockito.mock(SwaggerGeneratorContext.class),
        null);
    Swagger swagger = swaggerGenerator.getSwagger();
    // to test MediaType overwrite
    swagger.addConsumes(MediaType.APPLICATION_XML);
    swagger.addProduces(MediaType.TEXT_PLAIN);
    swaggerDefinitionProcessor.process(SwaggerTestTarget_EmptyMediaType.class.getAnnotation(SwaggerDefinition.class),
        swaggerGenerator);

    // empty media type should not be set, so keep consumes/produces not modified
    assertThat(swagger.getConsumes(), Matchers.contains(MediaType.APPLICATION_XML));
    assertThat(swagger.getProduces(), Matchers.contains(MediaType.TEXT_PLAIN));
  }

  @SwaggerDefinition(tags = {
      @Tag(name = "testTag", description = "desc", externalDocs = @ExternalDocs(value = "testValue", url = "testUrl"))
  },
      host = "127.0.0.1",
      schemes = {Scheme.HTTP, Scheme.HTTPS},
      info = @Info(title = "title", version = "version", description = "desc", contact = @Contact(name = "contactName"),
          license = @License(name = "licenseName")),
      consumes = {MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN},
      produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  private class SwaggerTestTarget {
  }

  @SwaggerDefinition(consumes = "", produces = "")
  private class SwaggerTestTarget_EmptyMediaType {
  }
}
