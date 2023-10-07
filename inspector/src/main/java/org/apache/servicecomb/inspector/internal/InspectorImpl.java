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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.resource.ClassPathStaticResourceHandler;
import org.apache.servicecomb.common.rest.resource.StaticResourceHandler;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.common.part.InputStreamPart;
import org.apache.servicecomb.foundation.common.utils.ClassLoaderScopeContext;
import org.apache.servicecomb.inspector.internal.swagger.SchemaFormat;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

@Path("/inspector")
public class InspectorImpl {
  private static final Logger LOGGER = LoggerFactory.getLogger(InspectorImpl.class);

  private final StaticResourceHandler resourceHandler = new ClassPathStaticResourceHandler();

  private Map<String, String> schemas;

  private String serviceName;

  @VisibleForTesting
  public Map<String, String> getSchemas() {
    return schemas;
  }

  public InspectorImpl setServiceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  public InspectorImpl setSchemas(Map<String, String> schemas) {
    this.schemas = schemas;
    return this;
  }

  // when work in servlet mode, should concat url prefix
  // otherwise swagger ide can not run online test
  //
  // ServiceComb consumer has not this problem
  // ServiceComb consumer not care for producer deploy with or without servlet
  public InspectorImpl correctBasePathForOnlineTest(SCBEngine scbEngine) {
    Transport restTransport = scbEngine.getTransportManager().findTransport(CoreConst.RESTFUL);
    if (restTransport == null ||
        !restTransport.getClass().getName()
            .equals("org.apache.servicecomb.transport.rest.servlet.ServletRestTransport")) {
      return this;
    }

    String urlPrefix = ClassLoaderScopeContext.getClassLoaderScopeProperty(DefinitionConst.URL_PREFIX);
    if (StringUtils.isEmpty(urlPrefix)) {
      return this;
    }

    for (Entry<String, String> entry : schemas.entrySet()) {
      OpenAPI swagger = SwaggerUtils.parseSwagger(entry.getValue());
      if (SwaggerUtils.getBasePath(swagger).startsWith(urlPrefix)) {
        continue;
      }

      SwaggerUtils.setBasePath(swagger, urlPrefix + SwaggerUtils.getBasePath(swagger));

      entry.setValue(SwaggerUtils.swaggerToString(swagger));
    }
    return this;
  }

  @Path("/schemas")
  @GET
  public Collection<String> getSchemaIds() {
    return schemas.keySet();
  }

  @Path("/download/schemas")
  @GET
  @ApiResponse(responseCode = "200", description = "", content =
  @Content(schema = @Schema(type = "string", format = "binary")))
  public Response downloadSchemas(@QueryParam("format") SchemaFormat format) {
    if (format == null) {
      format = SchemaFormat.SWAGGER;
    }

    // normally, schema will not be too big, just save them in memory temporarily
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(os)) {
      for (Entry<String, String> entry : schemas.entrySet()) {
        // begin writing a new ZIP entry, positions the stream to the start of the entry data
        zos.putNextEntry(new ZipEntry(entry.getKey() + format.getSuffix()));

        String content = entry.getValue();
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
      }
    } catch (Throwable e) {
      String msg = "failed to create schemas zip file, format=" + format + ".";
      LOGGER.error(msg, e);
      return Response.failResp(new InvocationException(Status.INTERNAL_SERVER_ERROR, msg));
    }

    Part part = new InputStreamPart(null, new ByteArrayInputStream(os.toByteArray()))
        .setSubmittedFileName(serviceName + format.getSuffix() + ".zip");
    return Response.ok(part);
  }

  @Path("/schemas/{schemaId}")
  @GET
  @ApiResponse(responseCode = "200", description = "", content =
  @Content(schema = @Schema(type = "string", format = "binary")))
  public Response getSchemaContentById(@PathParam("schemaId") String schemaId,
      @QueryParam("format") SchemaFormat format, @QueryParam("download") boolean download) {
    String swaggerContent = schemas.get(schemaId);
    if (swaggerContent == null) {
      return Response.failResp(new InvocationException(Status.NOT_FOUND, Status.NOT_FOUND.getReasonPhrase()));
    }

    if (format == null) {
      format = SchemaFormat.SWAGGER;
    }

    byte[] bytes = swaggerContent.getBytes(StandardCharsets.UTF_8);

    Part part = new InputStreamPart(null, new ByteArrayInputStream(bytes))
        .setSubmittedFileName(schemaId + format.getSuffix());

    Response response = Response.ok(part);
    if (!download) {
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline");
    }
    response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML);
    return response;
  }

  @Path("/{path : .+}")
  @GET
  @ApiResponse(responseCode = "200", description = "", content =
  @Content(schema = @Schema(type = "string", format = "binary")))
  public Response getStaticResource(@PathParam("path") String path) {
    return resourceHandler.handle(path);
  }
}
