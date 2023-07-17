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

package org.apache.servicecomb.common.rest.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.common.rest.codec.param.FormProcessorCreator.PartProcessor;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import org.apache.servicecomb.common.rest.definition.path.PathRegExp;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.utils.MimeTypesUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

@SuppressWarnings("rawtypes")
public class RestOperationMeta {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestOperationMeta.class);

  protected OperationMeta operationMeta;

  protected List<String> produces;

  protected boolean formData;

  // make sure if response is file
  protected boolean downloadFile;

  protected List<RestParam> paramList = new ArrayList<>();

  // key为参数名
  protected Map<String, RestParam> paramMap = new LinkedHashMap<>();

  protected List<String> fileKeys = new ArrayList<>();

  // key为数据类型，比如json之类
  private final Map<String, ProduceProcessor> produceProcessorMap = new LinkedHashMap<>();

  // 不一定等于mgr中的default，因为本operation可能不支持mgr中的default
  private ProduceProcessor defaultProcessor;

  protected String absolutePath;

  protected PathRegExp absolutePathRegExp;

  // 快速构建URL path
  private URLPathBuilder pathBuilder;

  public void init(OperationMeta operationMeta) {
    this.operationMeta = operationMeta;

    OpenAPI swagger = operationMeta.getSchemaMeta().getSwagger();
    Operation operation = operationMeta.getSwaggerOperation();
    this.produces =
        (operation.getResponses().get(SwaggerConst.SUCCESS_KEY) == null ||
            operation.getResponses().get(SwaggerConst.SUCCESS_KEY).getContent() == null) ?
            null : operation.getResponses().get(SwaggerConst.SUCCESS_KEY).getContent().keySet().stream().toList();

    this.downloadFile = checkDownloadFileFlag();
    this.createProduceProcessors();

    if (operation.getParameters() != null) {
      for (int swaggerParameterIdx = 0; swaggerParameterIdx < operation.getParameters().size(); swaggerParameterIdx++) {
        Parameter parameter = operation.getParameters().get(swaggerParameterIdx);
        Type type = operationMeta.getSwaggerProducerOperation() != null ? operationMeta.getSwaggerProducerOperation()
            .getSwaggerParameterTypes().get(parameter.getName()) : null;
        RestParam param = new RestParam(parameter, type);
        addParam(param);
      }
    }

    if (operation.getRequestBody() != null) {
      if (isFormParameters(operation)) {
        formData = true;
        Schema formSchema = formSchemas(operation);
        if (formSchema != null) {
          formSchema.getProperties().forEach((k, v) -> {
            addRestParamByName(operationMeta, (String) k, operation);
          });
        }
      } else {
        addRestParamByName(operationMeta,
            (String) operation.getRequestBody().getExtensions().get(SwaggerConst.EXT_BODY_NAME), operation);
      }
    }

    setAbsolutePath(concatPath(SwaggerUtils.getBasePath(swagger), operationMeta.getOperationPath()));
  }

  private void addRestParamByName(OperationMeta operationMeta, String name, Operation operation) {
    Type type = operationMeta.getSwaggerProducerOperation() != null ? operationMeta.getSwaggerProducerOperation()
        .getSwaggerParameterTypes().get(name) : null;
    type = correctFormBodyType(operation.getRequestBody(), type);
    RestParam param = new RestParam(name, operation.getRequestBody(), formData, type);
    addParam(param);
  }

  private boolean isFormParameters(Operation operation) {
    return operation.getRequestBody().getContent().get(SwaggerConst.FORM_MEDIA_TYPE) != null ||
        operation.getRequestBody().getContent().get(SwaggerConst.FILE_MEDIA_TYPE) != null;
  }

  private Schema formSchemas(Operation operation) {
    if (operation.getRequestBody().getContent().get(SwaggerConst.FORM_MEDIA_TYPE) != null) {
      return operation.getRequestBody().getContent().get(SwaggerConst.FORM_MEDIA_TYPE).getSchema();
    }
    return operation.getRequestBody().getContent().get(SwaggerConst.FILE_MEDIA_TYPE).getSchema();
  }

  /**
   * EdgeService cannot recognize the map type form body whose value type is String,
   * so there should be this additional setting.
   * @param parameter the swagger information of the parameter
   * @param type the resolved param type
   * @return the corrected param type
   */
  private Type correctFormBodyType(RequestBody parameter, Type type) {
    if (null != type || parameter == null) {
      return type;
    }
    if (parameter.getContent().get(MediaType.APPLICATION_JSON) == null
        || !(parameter.getContent().get(MediaType.APPLICATION_JSON).getSchema() instanceof MapSchema)) {
      return null;
    }
    return RestObjectMapperFactory.getRestObjectMapper().getTypeFactory()
        .constructMapType(Map.class, String.class, String.class);
  }

  public boolean isDownloadFile() {
    return downloadFile;
  }

  private boolean checkDownloadFileFlag() {
    ApiResponse response = operationMeta.getSwaggerOperation().getResponses().get(SwaggerConst.SUCCESS_KEY);
    return response != null && response.getContent() != null
        && response.getContent().get(SwaggerConst.FORM_MEDIA_TYPE) != null;
  }

  public boolean isFormData() {
    return formData;
  }

  public void setOperationMeta(OperationMeta operationMeta) {
    this.operationMeta = operationMeta;
  }

  /**
   * Concat the two paths to an absolute path, end of '/' added.
   *
   * e.g. "/" + "/ope" = /ope/
   * e.g. "/prefix" + "/ope" = /prefix/ope/
   */
  private String concatPath(String basePath, String operationPath) {
    return ("/" + nonNullify(basePath) + "/" + nonNullify(operationPath) + "/")
        .replaceAll("/{2,}", "/");
  }

  private String nonNullify(String path) {
    return path == null ? "" : path;
  }

  public String getAbsolutePath() {
    return this.absolutePath;
  }

  public void setAbsolutePath(String absolutePath) {
    this.absolutePath = absolutePath;
    this.absolutePathRegExp = createPathRegExp(absolutePath);
    this.pathBuilder = new URLPathBuilder(absolutePath, paramMap);
  }

  public PathRegExp getAbsolutePathRegExp() {
    return this.absolutePathRegExp;
  }

  public boolean isAbsoluteStaticPath() {
    return this.absolutePathRegExp.isStaticPath();
  }

  protected PathRegExp createPathRegExp(String path) {
    if (path == null || path.equals("")) {
      throw new Error("null rest url is not supported");
    }
    try {
      return new PathRegExp(path);
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
      return null;
    }
  }

  public RestParam getParamByName(String name) {
    return paramMap.get(name);
  }

  public RestParam getParamByIndex(int index) {
    return paramList.get(index);
  }

  public OperationMeta getOperationMeta() {
    return operationMeta;
  }

  protected void createProduceProcessors() {
    SwaggerProducerOperation producerOperation = operationMeta.getExtData(Const.PRODUCER_OPERATION);
    if (producerOperation != null && producerOperation.getProducerMethod() != null) {
      createProduceProcessors(producerOperation.getProducerMethod().getDeclaredAnnotations());
      return;
    }
    createProduceProcessors(null);
  }

  // serialViewClass is deterministic for each operation
  protected void createProduceProcessors(Annotation[] annotations) {
    if (annotations == null || annotations.length < 1) {
      doCreateProduceProcessors(null);
      return;
    }
    for (Annotation annotation : annotations) {
      if (annotation.annotationType() == JsonView.class) {
        Class<?>[] value = ((JsonView) annotation).value();
        if (value.length != 1) {
          throw new IllegalArgumentException(
              "@JsonView only supported for exactly 1 class argument ");
        }
        doCreateProduceProcessors(value[0]);
        return;
      }
    }
    doCreateProduceProcessors(null);
  }

  // 为operation创建支持的多种produce processor
  protected void doCreateProduceProcessors(Class<?> serialViewClass) {
    if (null == produces || produces.isEmpty()) {
      produceProcessorMap.putAll(
          ProduceProcessorManager.INSTANCE.getOrCreateAcceptMap(serialViewClass));
    } else {
      for (String produce : produces) {
        if (produce.contains(";")) {
          produce = produce.substring(0, produce.indexOf(";"));
        }
        ProduceProcessor processor = ProduceProcessorManager.INSTANCE.findProcessor(produce, serialViewClass);
        if (processor == null) {
          LOGGER.error("produce {} is not supported, operation={}.", produce,
              operationMeta.getMicroserviceQualifiedName());
          continue;
        }
        this.produceProcessorMap.put(produce, processor);
      }

      if (produceProcessorMap.isEmpty()) {
        produceProcessorMap.put(ProduceProcessorManager.DEFAULT_TYPE,
            ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(serialViewClass));
      }
    }

    if (produceProcessorMap.get(ProduceProcessorManager.DEFAULT_TYPE) != null) {
      defaultProcessor = produceProcessorMap.get(ProduceProcessorManager.DEFAULT_TYPE);
    } else {
      defaultProcessor = produceProcessorMap.values().stream().findFirst().get();
    }
    produceProcessorMap.putIfAbsent(MediaType.WILDCARD, defaultProcessor);
  }

  public URLPathBuilder getPathBuilder() {
    return this.pathBuilder;
  }

  public List<RestParam> getParamList() {
    return paramList;
  }

  private void addParam(RestParam param) {
    if (param.getParamProcessor() instanceof PartProcessor) {
      fileKeys.add(param.getParamName());
    }
    paramList.add(param);
    paramMap.put(param.getParamName(), param);
  }

  public ProduceProcessor findProduceProcessor(String type) {
    return this.produceProcessorMap.get(type);
  }

  // 选择与accept匹配的produce processor或者缺省的
  public ProduceProcessor ensureFindProduceProcessor(HttpServletRequestEx requestEx) {
    String acceptType = requestEx.getHeader(HttpHeaders.ACCEPT);
    return ensureFindProduceProcessor(acceptType);
  }

  public ProduceProcessor ensureFindProduceProcessor(String acceptType) {
    if (downloadFile) {
      //do not check accept type, when the produces of provider is text/plain there will return text/plain processor
      //when the produces of provider is application/json there will return the application/json processor
      //so do not care what accept type the consumer will set.
      return this.produceProcessorMap.get(MediaType.WILDCARD);
    }
    if (StringUtils.isEmpty(acceptType)) {
      return defaultProcessor;
    }
    List<String> mimeTypes = MimeTypesUtils.getSortedAcceptableMimeTypes(acceptType.toLowerCase(Locale.US));
    for (String mime : mimeTypes) {
      ProduceProcessor processor = this.produceProcessorMap.get(mime);
      if (null != processor) {
        return processor;
      }
    }

    return null;
  }

  public String getHttpMethod() {
    return operationMeta.getHttpMethod();
  }

  public List<String> getFileKeys() {
    return fileKeys;
  }

  public List<String> getProduces() {
    return produces;
  }
}
