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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.Part;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.config.inject.ConfigObjectFactory;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.inspector.internal.swagger.SchemaFormat;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class TestInspectorImpl {
  static Map<String, String> schemas = new LinkedHashMap<>();

  static InspectorConfig inspectorConfig;

  static InspectorImpl inspector;

  @BeforeClass
  public static void setup() throws IOException {
    ArchaiusUtils.resetConfig();
    inspectorConfig = new ConfigObjectFactory().create(InspectorConfig.class);
    inspector = new InspectorImpl(inspectorConfig, schemas);

    schemas.put("schema1", IOUtils
        .toString(TestInspectorImpl.class.getClassLoader().getResource("schema1.yaml"), Charset.defaultCharset()));
    schemas.put("schema2", IOUtils
        .toString(TestInspectorImpl.class.getClassLoader().getResource("schema2.yaml"), Charset.defaultCharset()));
  }

  @AfterClass
  public static void teardown() {
    ArchaiusUtils.resetConfig();
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

      result.put(zipEntry.getName(), IOUtils.toString(zis, Charset.defaultCharset()));
    }
  }

  @Test
  public void getSchemaIds() {
    Assert.assertThat(inspector.getSchemaIds(), Matchers.contains("schema1", "schema2"));
  }

  @Test
  public void downloadSchemas_default_to_swagger(@Mocked Microservice microservice) throws IOException {
    testDownloadSchemasSwagger(microservice, null);
  }

  @Test
  public void downloadSchemas_swagger(@Mocked Microservice microservice) throws IOException {
    testDownloadSchemasSwagger(microservice, SchemaFormat.SWAGGER);
  }

  private void testDownloadSchemasSwagger(Microservice microservice, SchemaFormat format) throws IOException {
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getMicroservice();
        result = microservice;
        microservice.getServiceName();
        result = "ms";
      }
    };

    Response response = inspector.downloadSchemas(format);
    Part part = response.getResult();
    Assert.assertEquals("ms.yaml.zip", part.getSubmittedFileName());

    try (InputStream is = part.getInputStream()) {
      Map<String, String> unziped = unzip(is);

      Assert.assertEquals(schemas.size(), unziped.size());
      Assert.assertEquals(schemas.get("schema1"), unziped.get("schema1.yaml"));
      Assert.assertEquals(schemas.get("schema2"), unziped.get("schema2.yaml"));
    }
  }

  @Test
  public void downloadSchemas_html(@Mocked Microservice microservice) throws IOException {
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getMicroservice();
        result = microservice;
        microservice.getServiceName();
        result = "ms";
      }
    };

    Response response = inspector.downloadSchemas(SchemaFormat.HTML);
    Part part = response.getResult();
    Assert.assertEquals("ms.html.zip", part.getSubmittedFileName());

    try (InputStream is = part.getInputStream()) {
      Map<String, String> unziped = unzip(is);

      Assert.assertEquals(schemas.size(), unziped.size());
      Assert.assertTrue(unziped.get("schema1.html").endsWith("</html>"));
      Assert.assertTrue(unziped.get("schema2.html").endsWith("</html>"));
    }
  }

  @Test
  public void downloadSchemas_failed() {
    SchemaFormat format = SchemaFormat.SWAGGER;
    new Expectations(format) {
      {
        format.getSuffix();
        result = new RuntimeExceptionWithoutStackTrace("zip failed.");
      }
    };

    try (LogCollector logCollector = new LogCollector()) {
      Response response = inspector.downloadSchemas(format);

      Assert.assertEquals("failed to create schemas zip file, format=SWAGGER.",
          logCollector.getLastEvents().getMessage());

      InvocationException invocationException = response.getResult();
      Assert.assertEquals(Status.INTERNAL_SERVER_ERROR, invocationException.getStatus());
      Assert.assertEquals("failed to create schemas zip file, format=SWAGGER.",
          ((CommonExceptionData) invocationException.getErrorData()).getMessage());
      Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
      Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getReasonPhrase(), response.getReasonPhrase());
    }
  }

  @Test
  public void getSchemaContentById_notExist() {
    Response response = inspector.getSchemaContentById("notExist", null, false);

    InvocationException invocationException = response.getResult();
    Assert.assertEquals(Status.NOT_FOUND, invocationException.getStatus());
    Assert.assertEquals(Status.NOT_FOUND.getReasonPhrase(),
        ((CommonExceptionData) invocationException.getErrorData()).getMessage());
    Assert.assertEquals(404, response.getStatusCode());
    Assert.assertEquals("Not Found", response.getReasonPhrase());
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
    Assert.assertEquals(schemaId + ".yaml", part.getSubmittedFileName());
    Assert.assertEquals("inline", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    Assert.assertEquals(MediaType.TEXT_HTML, response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));

    try (InputStream is = part.getInputStream()) {
      Assert.assertEquals(schemas.get(schemaId), IOUtils.toString(is, Charset.defaultCharset()));
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
    Assert.assertEquals(schemaId + ".yaml", part.getSubmittedFileName());
    Assert.assertNull(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    Assert.assertEquals(MediaType.TEXT_HTML, response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));

    try (InputStream is = part.getInputStream()) {
      Assert.assertEquals(schemas.get(schemaId), IOUtils.toString(is, Charset.defaultCharset()));
    }
  }

  @Test
  public void getSchemaContentById_view_html() throws IOException {
    testViewHtmlById("schema1");
    testViewHtmlById("schema2");
  }

  private void testViewHtmlById(String schemaId) throws IOException {
    Response response = inspector.getSchemaContentById(schemaId, SchemaFormat.HTML, false);

    Part part = response.getResult();
    Assert.assertEquals(schemaId + ".html", part.getSubmittedFileName());
    Assert.assertEquals("inline", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    Assert.assertEquals(MediaType.TEXT_HTML, response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));

    try (InputStream is = part.getInputStream()) {
      Assert.assertTrue(IOUtils.toString(is, Charset.defaultCharset()).endsWith("</html>"));
    }
  }

  @Test
  public void getSchemaContentById_download_html() throws IOException {
    testDownloadHtmlById("schema1");
    testDownloadHtmlById("schema2");
  }

  private void testDownloadHtmlById(String schemaId) throws IOException {
    Response response = inspector.getSchemaContentById(schemaId, SchemaFormat.HTML, true);

    Part part = response.getResult();
    Assert.assertEquals(schemaId + ".html", part.getSubmittedFileName());
    Assert.assertNull(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    Assert.assertEquals(MediaType.TEXT_HTML, response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));

    try (InputStream is = part.getInputStream()) {
      Assert.assertTrue(IOUtils.toString(is, Charset.defaultCharset()).endsWith("</html>"));
    }
  }

  @Test
  public void getStaticResource_notExist() throws IOException {
    Response response = inspector.getStaticResource("notExist");

    InvocationException invocationException = response.getResult();
    Assert.assertEquals(Status.NOT_FOUND, invocationException.getStatus());
    Assert.assertEquals(Status.NOT_FOUND.getReasonPhrase(),
        ((CommonExceptionData) invocationException.getErrorData()).getMessage());
    Assert.assertEquals(404, response.getStatusCode());
    Assert.assertEquals("Not Found", response.getReasonPhrase());
  }

  @Test
  public void getStaticResource() throws IOException {
    Response response = inspector.getStaticResource("index.html");

    Part part = response.getResult();
    Assert.assertEquals("inline", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    Assert.assertEquals(MediaType.TEXT_HTML, response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));

    try (InputStream is = part.getInputStream()) {
      Assert.assertTrue(IOUtils.toString(is, Charset.defaultCharset()).endsWith("</html>"));
    }
  }
}
