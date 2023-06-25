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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.resource.ClassPathStaticResourceHandler;
import org.apache.servicecomb.common.rest.resource.StaticResourceHandler;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.priority.PriorityProperty;
import org.apache.servicecomb.config.priority.PriorityPropertyFactory;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.common.part.InputStreamPart;
import org.apache.servicecomb.foundation.common.utils.ClassLoaderScopeContext;
import org.apache.servicecomb.inspector.internal.model.DynamicPropertyView;
import org.apache.servicecomb.inspector.internal.model.PriorityPropertyView;
import org.apache.servicecomb.inspector.internal.swagger.SchemaFormat;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.config.DynamicProperty;

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

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final StaticResourceHandler resourceHandler = new ClassPathStaticResourceHandler();

  private Map<String, String> schemas;

  private PriorityPropertyFactory propertyFactory;

  public InspectorImpl setPropertyFactory(PriorityPropertyFactory propertyFactory) {
    this.propertyFactory = propertyFactory;
    return this;
  }

  @VisibleForTesting
  public Map<String, String> getSchemas() {
    return schemas;
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
    Transport restTransport = scbEngine.getTransportManager().findTransport(Const.RESTFUL);
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
        .setSubmittedFileName(
            RegistrationManager.INSTANCE.getMicroservice().getServiceName() + format.getSuffix() + ".zip");
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

  @Path("/dynamicProperties")
  @GET
  public List<DynamicPropertyView> dynamicProperties() {
    List<DynamicPropertyView> views = new ArrayList<>();
    for (DynamicProperty property : ConfigUtil.getAllDynamicProperties().values()) {
      views.add(createDynamicPropertyView(property));
    }

    // show more callback first, because maybe there is memory leak problem
    // show recently changed second
    // and sort by key
    views.sort(Comparator
        .comparing(DynamicPropertyView::getCallbackCount)
        .thenComparing(DynamicPropertyView::getChangedTime).reversed()
        .thenComparing(DynamicPropertyView::getKey));
    return views;
  }

  private DynamicPropertyView createDynamicPropertyView(DynamicProperty property) {
    DynamicPropertyView view = new DynamicPropertyView();
    view.setKey(property.getName());
    view.setValue(property.getString());

    if (property.getChangedTimestamp() != 0) {
      LocalDateTime localDatetime = LocalDateTime
          .ofInstant(Instant.ofEpochMilli(property.getChangedTimestamp()), ZoneId.systemDefault());
      view.setChangedTime(localDatetime.format(FORMATTER));
    }

    view.setCallbackCount(ConfigUtil.getCallbacks(property).size());
    return view;
  }

  @Path("/priorityProperties")
  @GET
  public List<PriorityPropertyView> priorityProperties() {
    List<PriorityPropertyView> views = new ArrayList<>();
    propertyFactory.getProperties()
        .forEach(p -> views.add(createPriorityPropertyView(p)));
    return views;
  }

  private PriorityPropertyView createPriorityPropertyView(PriorityProperty<?> priorityProperty) {
    PriorityPropertyView view = new PriorityPropertyView();
    view.setDynamicProperties(new ArrayList<>());
    for (DynamicProperty property : priorityProperty.getProperties()) {
      view.getDynamicProperties().add(createDynamicPropertyView(property));
    }
    view.setDefaultValue(String.valueOf(priorityProperty.getDefaultValue()));
    view.setValue(String.valueOf(priorityProperty.getValue()));
    return view;
  }
}
