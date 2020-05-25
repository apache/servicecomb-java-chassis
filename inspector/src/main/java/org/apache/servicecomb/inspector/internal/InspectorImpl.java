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
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.Part;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.resource.ClassPathStaticResourceHandler;
import org.apache.servicecomb.common.rest.resource.StaticResourceHandler;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.priority.PriorityProperty;
import org.apache.servicecomb.config.priority.PriorityPropertyManager;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.common.part.InputStreamPart;
import org.apache.servicecomb.foundation.common.utils.ClassLoaderScopeContext;
import org.apache.servicecomb.inspector.internal.model.DynamicPropertyView;
import org.apache.servicecomb.inspector.internal.model.PriorityPropertyView;
import org.apache.servicecomb.inspector.internal.swagger.AppendStyleProcessor;
import org.apache.servicecomb.inspector.internal.swagger.SchemaFormat;
import org.apache.servicecomb.serviceregistry.RegistrationManager;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Asciidoctor.Factory;
import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Ordering;
import com.netflix.config.DynamicProperty;

import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.Swagger2MarkupConverter.Builder;
import io.github.swagger2markup.builder.Swagger2MarkupConfigBuilder;
import io.swagger.annotations.ApiResponse;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;

@Path("/inspector")
public class InspectorImpl {
  private static final Logger LOGGER = LoggerFactory.getLogger(InspectorImpl.class);

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final SCBEngine scbEngine;

  private InspectorConfig inspectorConfig;

  private Map<String, String> schemas;

  private volatile Asciidoctor asciidoctor;

  private StaticResourceHandler resourceHandler = new ClassPathStaticResourceHandler();

  private PriorityPropertyManager priorityPropertyManager;

  public InspectorImpl(SCBEngine scbEngine, InspectorConfig inspectorConfig, Map<String, String> schemas) {
    this.scbEngine = scbEngine;
    this.inspectorConfig = inspectorConfig;
    this.schemas = new LinkedHashMap<>(schemas);

    correctBasePathForOnlineTest();
  }

  public SCBEngine getScbEngine() {
    return scbEngine;
  }

  public InspectorConfig getInspectorConfig() {
    return inspectorConfig;
  }

  // when work in servlet mode, should concat url prefix
  // otherwise swagger ide can not run online test
  //
  // ServiceComb consumer has not this problem
  // ServiceComb consumer not care for producer deploy with or without servlet
  private void correctBasePathForOnlineTest() {
    Transport restTransport = scbEngine.getTransportManager().findTransport(Const.RESTFUL);
    if (restTransport == null ||
        !restTransport.getClass().getName()
            .equals("org.apache.servicecomb.transport.rest.servlet.ServletRestTransport")) {
      return;
    }

    String urlPrefix = ClassLoaderScopeContext.getClassLoaderScopeProperty(DefinitionConst.URL_PREFIX);
    if (StringUtils.isEmpty(urlPrefix)) {
      return;
    }

    for (Entry<String, String> entry : schemas.entrySet()) {
      Swagger swagger = SwaggerUtils.parseSwagger(entry.getValue());
      if (swagger.getBasePath().startsWith(urlPrefix)) {
        continue;
      }

      swagger.setBasePath(urlPrefix + swagger.getBasePath());

      entry.setValue(SwaggerUtils.swaggerToString(swagger));
    }
  }

  public void setPriorityPropertyManager(PriorityPropertyManager priorityPropertyManager) {
    this.priorityPropertyManager = priorityPropertyManager;
  }

  @Path("/schemas")
  @GET
  public Collection<String> getSchemaIds() {
    return schemas.keySet();
  }

  @Path("/download/schemas")
  @GET
  @ApiResponse(code = 200, message = "", response = File.class)
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
        if (SchemaFormat.HTML.equals(format)) {
          content = swaggerToHtml(content);
        }
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
  @ApiResponse(code = 200, message = "", response = File.class)
  public Response getSchemaContentById(@PathParam("schemaId") String schemaId,
      @QueryParam("format") SchemaFormat format, @QueryParam("download") boolean download) {
    String swaggerContent = schemas.get(schemaId);
    if (swaggerContent == null) {
      return Response.failResp(new InvocationException(Status.NOT_FOUND, Status.NOT_FOUND.getReasonPhrase()));
    }

    if (format == null) {
      format = SchemaFormat.SWAGGER;
    }

    byte[] bytes;
    if (SchemaFormat.HTML.equals(format)) {
      String html = swaggerToHtml(swaggerContent);
      bytes = html.getBytes(StandardCharsets.UTF_8);
    } else {
      bytes = swaggerContent.getBytes(StandardCharsets.UTF_8);
    }

    Part part = new InputStreamPart(null, new ByteArrayInputStream(bytes))
        .setSubmittedFileName(schemaId + format.getSuffix());

    Response response = Response.ok(part);
    if (!download) {
      response.getHeaders().addHeader(HttpHeaders.CONTENT_DISPOSITION, "inline");
    }
    response.getHeaders().addHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML);
    return response;
  }

  // swagger not support cookie parameter
  // so if swaggerContent contains cookie parameter, will cause problem.
  private String swaggerToHtml(String swaggerContent) {
    if (asciidoctor == null) {
      synchronized (this) {
        if (asciidoctor == null) {
          // very slow, need a few seconds
          LOGGER.info("create AsciiDoctor start.");
          asciidoctor = Factory.create();
          asciidoctor.javaExtensionRegistry().docinfoProcessor(AppendStyleProcessor.class);
          LOGGER.info("create AsciiDoctor end.");
        }
      }
    }

    // swagger to markup
    Builder markupBuilder = Swagger2MarkupConverter.from(SwaggerUtils.parseSwagger(swaggerContent));
    // default not support cookie parameter
    // so must customize config
    Swagger2MarkupConfig markupConfig = new Swagger2MarkupConfigBuilder()
        .withParameterOrdering(Ordering
            .explicit("path", "query", "header", "cookie", "formData", "body")
            .onResultOf(Parameter::getIn))
        .build();
    String markup = markupBuilder.withConfig(markupConfig).build().toString();

    // markup to html
    OptionsBuilder builder = OptionsBuilder.options();
    builder.docType("book")
        .backend("html5")
        .headerFooter(true)
        .safe(SafeMode.UNSAFE)
        .attributes(AttributesBuilder.attributes()
            .attribute("toclevels", 3)
            .attribute(Attributes.TOC_2, true)
            .attribute(Attributes.TOC_POSITION, "left")
            .attribute(Attributes.LINK_CSS, true)
            .attribute(Attributes.STYLESHEET_NAME, inspectorConfig.getAsciidoctorCss())
            .attribute(Attributes.SECTION_NUMBERS, true)
            .attribute(Attributes.SECT_NUM_LEVELS, 4));
    return asciidoctor.convert(markup, builder.asMap());
  }

  @Path("/{path : .+}")
  @GET
  @ApiResponse(code = 200, message = "", response = File.class)
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
    priorityPropertyManager.getConfigObjectMap().values().stream()
        .flatMap(Collection::stream)
        .forEach(p -> views.add(createPriorityPropertyView(p)));
    priorityPropertyManager.getPriorityPropertyMap().keySet().forEach(p ->
        views.add(createPriorityPropertyView(p)));
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
