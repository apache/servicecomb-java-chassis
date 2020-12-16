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

import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.hamcrest.Matchers;
import org.junit.Test;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.BasicAuthDefinition;
import io.swagger.annotations.Contact;
import io.swagger.annotations.ExternalDocs;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.OAuth2Definition;
import io.swagger.annotations.OAuth2Definition.Flow;
import io.swagger.annotations.Scope;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.SwaggerDefinition.Scheme;
import io.swagger.annotations.Tag;
import io.swagger.models.Swagger;

public class SwaggerDefinitionProcessorTest {
  @Api(consumes = MediaType.APPLICATION_XML, produces = MediaType.APPLICATION_XML)
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

  @Api(consumes = MediaType.APPLICATION_XML, produces = MediaType.TEXT_PLAIN)
  @SwaggerDefinition(consumes = "", produces = "")
  private class SwaggerTestTarget_EmptyMediaType {
  }

  @SwaggerDefinition()
  private class EmptySwaggerDefinition {
  }

  @SwaggerDefinition(securityDefinition = @SecurityDefinition(
      oAuth2Definitions = {
          @OAuth2Definition(key = "", flow = Flow.IMPLICIT),
          @OAuth2Definition(key = "oauth2-only-flow", flow = Flow.IMPLICIT),
          @OAuth2Definition(key = "oauth2-desc", description = "desc", flow = Flow.PASSWORD),
          @OAuth2Definition(key = "oauth2-authorizationUrl", authorizationUrl = "url", flow = Flow.PASSWORD),
          @OAuth2Definition(key = "oauth2-tokenUrl", tokenUrl = "url", flow = Flow.PASSWORD),
          @OAuth2Definition(key = "oauth2-scope", flow = Flow.PASSWORD,
              scopes = {
                  @Scope(name = "", description = ""),
                  @Scope(name = "", description = "desc"),
                  @Scope(name = "scope-1", description = ""),
                  @Scope(name = "scope-2", description = "desc")
              }),
      })
  )
  private class SecurityOAuth2 {
  }

  @SwaggerDefinition(securityDefinition = @SecurityDefinition(
      apiKeyAuthDefinitions = {
          @ApiKeyAuthDefinition(key = "", in = ApiKeyLocation.HEADER, name = "h1"),
          @ApiKeyAuthDefinition(key = "apikey-no-name", in = ApiKeyLocation.QUERY, name = ""),
          @ApiKeyAuthDefinition(key = "apikey-no-desc", in = ApiKeyLocation.QUERY, name = "q1"),
          @ApiKeyAuthDefinition(key = "apikey-desc", description = "desc", in = ApiKeyLocation.QUERY, name = "q2")
      }
  ))
  private class SecurityApiKey {
  }

  @SwaggerDefinition(securityDefinition = @SecurityDefinition(
      basicAuthDefinitions = {
          @BasicAuthDefinition(key = ""),
          @BasicAuthDefinition(key = "basic-no-desc"),
          @BasicAuthDefinition(key = "basic-desc", description = "desc")
      }
  ))
  private class SecurityBasic {
  }

  @Test
  public void testProcess() {
    Swagger swagger = SwaggerGenerator.generate(SwaggerTestTarget.class);

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
    Swagger swagger = SwaggerGenerator.generate(SwaggerTestTarget_EmptyMediaType.class);
    assertThat(swagger.getConsumes(), Matchers.contains(MediaType.APPLICATION_XML));
    assertThat(swagger.getProduces(), Matchers.contains(MediaType.TEXT_PLAIN));
  }

  @Test
  public void emptySwaggerDefinitionMediaType() {
    UnitTestSwaggerUtils.testSwagger("schemas/emptySwaggerDefinition.yaml", EmptySwaggerDefinition.class);
  }

  @Test
  public void should_process_security_oauth2() {
    UnitTestSwaggerUtils.testSwagger("schemas/security-oauth2.yaml", SecurityOAuth2.class);
  }

  @Test
  public void should_process_security_apikey() {
    UnitTestSwaggerUtils.testSwagger("schemas/security-apikey.yaml", SecurityApiKey.class);
  }

  @Test
  public void should_process_security_basic() {
    UnitTestSwaggerUtils.testSwagger("schemas/security-basic.yaml", SecurityBasic.class);
  }
}
