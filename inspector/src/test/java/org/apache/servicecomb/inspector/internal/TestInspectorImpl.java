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
package org.apache.servicecomb.inspector.internal;

import static org.apache.servicecomb.core.CoreConst.RESTFUL;
import static org.apache.servicecomb.core.SCBEngine.CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC;
import static org.apache.servicecomb.core.SCBEngine.DEFAULT_TURN_DOWN_STATUS_WAIT_SEC;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.utils.ClassLoaderScopeContext;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.inspector.internal.swagger.SchemaFormat;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.apache.servicecomb.transport.rest.servlet.ServletRestTransport;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import io.vertx.core.file.impl.FileResolverImpl;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

public class TestInspectorImpl {
  static Map<String, String> schemas = new LinkedHashMap<>();

  static InspectorImpl inspector;

  @BeforeAll
  public static void setup() throws IOException {
    schemas.put("schema1", IOUtils
        .toString(TestInspectorImpl.class.getClassLoader().getResource("schema1.yaml"), StandardCharsets.UTF_8));
    schemas.put("schema2", IOUtils
        .toString(TestInspectorImpl.class.getClassLoader().getResource("schema2.yaml"), StandardCharsets.UTF_8));
  }

  Environment environment = Mockito.mock(Environment.class);

  @BeforeEach
  public void setUp() {
    inspector = initInspector(null);
    LegacyPropertyFactory.setEnvironment(environment);
  }

  private InspectorImpl initInspector(String urlPrefix) {
    SCBEngine scbEngine = SCBBootstrap.createSCBEngineForTest();
    scbEngine.setEnvironment(environment);
    Mockito.when(environment.getProperty(CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC,
        long.class, DEFAULT_TURN_DOWN_STATUS_WAIT_SEC)).thenReturn(DEFAULT_TURN_DOWN_STATUS_WAIT_SEC);
    scbEngine.getTransportManager().clearTransportBeforeInit();

    if (StringUtils.isNotEmpty(urlPrefix)) {
      Map<String, Transport> transportMap = scbEngine.getTransportManager().getTransportMap();
      ServletRestTransport servletRestTransport = new ServletRestTransport();
      servletRestTransport.setEnvironment(environment);
      transportMap.put(RESTFUL, servletRestTransport);
      ClassLoaderScopeContext.setClassLoaderScopeProperty(DefinitionConst.URL_PREFIX, urlPrefix);
    }

    scbEngine.run();
    InspectorImpl inspector = new InspectorImpl()
        .setSchemas(schemas);
    inspector.correctBasePathForOnlineTest(scbEngine);
    return inspector;
  }

  @AfterAll
  public static void teardown() {
    SCBEngine.getInstance().destroy();
    ClassLoaderScopeContext.clearClassLoaderScopeProperty();
  }

  private Map<String, String> unzip(InputStream is) throws IOException {
    Map<String, String> result = new LinkedHashMap<>();
    ZipInputStream zis = new ZipInputStream(is);
    for (; ; ) {
      ZipEntry zipEntry = zis.getNextEntry();
      if (zipEntry == null) {
        zis.close();
        return result;
      }

      result.put(zipEntry.getName(), IOUtils.toString(zis, StandardCharsets.UTF_8));
    }
  }

  @Test
  public void getSchemaIds() {
    MatcherAssert.assertThat(inspector.getSchemaIds(), Matchers.contains("schema1", "schema2"));
  }

  @Test
  public void downloadSchemas_default_to_swagger() throws IOException {
    testDownloadSchemasSwagger(null);
  }

  @Test
  public void downloadSchemas_swagger() throws IOException {
    testDownloadSchemasSwagger(SchemaFormat.SWAGGER);
  }

  private void testDownloadSchemasSwagger(SchemaFormat format) throws IOException {
    inspector.setServiceName("ms");

    Response response = inspector.downloadSchemas(format);
    Part part = response.getResult();
    Assertions.assertEquals("ms.yaml.zip", part.getSubmittedFileName());

    try (InputStream is = part.getInputStream()) {
      Map<String, String> unziped = unzip(is);

      Assertions.assertEquals(schemas.size(), unziped.size());
      Assertions.assertEquals(schemas.get("schema1"), unziped.get("schema1.yaml"));
      Assertions.assertEquals(schemas.get("schema2"), unziped.get("schema2.yaml"));
    }
  }

  @Test
  @EnabledForJreRange(min = JRE.JAVA_9)
  public void downloadSchemas_failed() {
    SchemaFormat format = Mockito.spy(SchemaFormat.SWAGGER);
    Mockito.doThrow(new RuntimeExceptionWithoutStackTrace("zip failed.")).when(format).getSuffix();

    try (LogCollector logCollector = new LogCollector()) {
      Response response = inspector.downloadSchemas(format);

      Assertions.assertEquals("failed to create schemas zip file, format=SWAGGER.",
          logCollector.getLastEvents().getMessage().getFormattedMessage());

      InvocationException invocationException = response.getResult();
      Assertions.assertEquals(Status.INTERNAL_SERVER_ERROR, invocationException.getStatus());
      Assertions.assertEquals("failed to create schemas zip file, format=SWAGGER.",
          ((CommonExceptionData) invocationException.getErrorData()).getMessage());
      Assertions.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
      Assertions.assertEquals(Status.INTERNAL_SERVER_ERROR.getReasonPhrase(), response.getReasonPhrase());
    }
  }

  @Test
  public void getSchemaContentById_notExist() {
    Response response = inspector.getSchemaContentById("notExist", null, false);

    InvocationException invocationException = response.getResult();
    Assertions.assertEquals(Status.NOT_FOUND, invocationException.getStatus());
    Assertions.assertEquals(Status.NOT_FOUND.getReasonPhrase(),
        ((CommonExceptionData) invocationException.getErrorData()).getMessage());
    Assertions.assertEquals(404, response.getStatusCode());
    Assertions.assertEquals("Not Found", response.getReasonPhrase());
  }

  @Test
  public void getSchemaContentById_view_swagger() throws IOException {
    testViewSwaggerById("schema1", null);
    testViewSwaggerById("schema1", SchemaFormat.SWAGGER);
    testViewSwaggerById("schema2", null);
    testViewSwaggerById("schema2", SchemaFormat.SWAGGER);
  }

  private void testViewSwaggerById(String schemaId, SchemaFormat format) throws IOException {
    Response response = inspector.getSchemaContentById(schemaId, format, false);

    Part part = response.getResult();
    Assertions.assertEquals(schemaId + ".yaml", part.getSubmittedFileName());
    Assertions.assertEquals("inline", response.getHeader(HttpHeaders.CONTENT_DISPOSITION));
    Assertions.assertEquals(MediaType.TEXT_HTML, response.getHeader(HttpHeaders.CONTENT_TYPE));

    try (InputStream is = part.getInputStream()) {
      Assertions.assertEquals(schemas.get(schemaId), IOUtils.toString(is, StandardCharsets.UTF_8));
    }
  }

  @Test
  public void getSchemaContentById_download_swagger() throws IOException {
    testDownloadSwaggerById("schema1", null);
    testDownloadSwaggerById("schema1", SchemaFormat.SWAGGER);
    testDownloadSwaggerById("schema2", null);
    testDownloadSwaggerById("schema2", SchemaFormat.SWAGGER);
  }

  private void testDownloadSwaggerById(String schemaId, SchemaFormat format) throws IOException {
    Response response = inspector.getSchemaContentById(schemaId, format, true);

    Part part = response.getResult();
    Assertions.assertEquals(schemaId + ".yaml", part.getSubmittedFileName());
    Assertions.assertNull(response.getHeader(HttpHeaders.CONTENT_DISPOSITION));
    Assertions.assertEquals(MediaType.TEXT_HTML, response.getHeader(HttpHeaders.CONTENT_TYPE));

    try (InputStream is = part.getInputStream()) {
      Assertions.assertEquals(schemas.get(schemaId), IOUtils.toString(is, StandardCharsets.UTF_8));
    }
  }

  @Test
  public void getStaticResource_notExist() throws IOException {
    Response response = inspector.getStaticResource("notExist");

    InvocationException invocationException = response.getResult();
    Assertions.assertEquals(Status.NOT_FOUND, invocationException.getStatus());
    Assertions.assertEquals(Status.NOT_FOUND.getReasonPhrase(),
        ((CommonExceptionData) invocationException.getErrorData()).getMessage());
    Assertions.assertEquals(404, response.getStatusCode());
    Assertions.assertEquals("Not Found", response.getReasonPhrase());
  }

  @Test
  public void getStaticResource() throws IOException {
    Response response = inspector.getStaticResource("index.html");

    Part part = response.getResult();
    Assertions.assertEquals("inline", response.getHeader(HttpHeaders.CONTENT_DISPOSITION));
    Assertions.assertEquals(MediaType.TEXT_HTML, response.getHeader(HttpHeaders.CONTENT_TYPE));

    try (InputStream is = part.getInputStream()) {
      Assertions.assertTrue(IOUtils.toString(is, StandardCharsets.UTF_8).endsWith("</html>"));
    }
  }

  @Test
  public void urlPrefix() {
    Mockito.when(environment.getProperty("servicecomb.transport.eventloop.size", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty(FileResolverImpl.DISABLE_CP_RESOLVING_PROP_NAME, boolean.class, true))
        .thenReturn(true);
    InspectorImpl inspector = initInspector("/webroot/rest");

    Map<String, String> schemas = inspector.getSchemas();
    Assertions.assertTrue(schemas.get("schema1").indexOf("/webroot/rest/metrics") > 0);
  }
}
