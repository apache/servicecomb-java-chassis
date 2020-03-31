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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.common.rest.codec.param.FormProcessorCreator.PartProcessor;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import org.apache.servicecomb.common.rest.definition.path.PathRegExp;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.utils.MimeTypesUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.FileProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;

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
  private Map<String, ProduceProcessor> produceProcessorMap = new LinkedHashMap<>();

  // 不一定等于mgr中的default，因为本operation可能不支持mgr中的default
  private ProduceProcessor defaultProcessor;

  protected String absolutePath;

  protected PathRegExp absolutePathRegExp;

  // 快速构建URL path
  private URLPathBuilder pathBuilder;

  public void init(OperationMeta operationMeta) {
    this.operationMeta = operationMeta;

    Swagger swagger = operationMeta.getSchemaMeta().getSwagger();
    Operation operation = operationMeta.getSwaggerOperation();
    this.produces = operation.getProduces();
    if (produces == null) {
      this.produces = swagger.getProduces();
    }

    this.downloadFile = checkDownloadFileFlag();
    this.createProduceProcessors();

    // 初始化所有rest param
    for (int swaggerParameterIdx = 0; swaggerParameterIdx < operation.getParameters().size(); swaggerParameterIdx++) {
      Parameter parameter = operation.getParameters().get(swaggerParameterIdx);

      if ("formData".equals(parameter.getIn())) {
        formData = true;
      }

      Type type = operationMeta.getSwaggerProducerOperation() != null ? operationMeta.getSwaggerProducerOperation()
          .getSwaggerParameterTypes().get(parameter.getName()) : null;
      type = correctFormBodyType(parameter, type);
      RestParam param = new RestParam(parameter, type);
      addParam(param);
    }

    setAbsolutePath(concatPath(swagger.getBasePath(), operationMeta.getOperationPath()));
  }

  /**
   * EdgeService cannot recognize the map type form body whose value type is String,
   * so there should be this additional setting.
   * @param parameter the swagger information of the parameter
   * @param type the resolved param type
   * @return the corrected param type
   */
  private Type correctFormBodyType(Parameter parameter, Type type) {
    if (null != type || !(parameter instanceof BodyParameter)) {
      return type;
    }
    final BodyParameter bodyParameter = (BodyParameter) parameter;
    if (!(bodyParameter.getSchema() instanceof ModelImpl)) {
      return type;
    }
    final Property additionalProperties = ((ModelImpl) bodyParameter.getSchema()).getAdditionalProperties();
    if (additionalProperties instanceof StringProperty) {
      type = RestObjectMapperFactory.getRestObjectMapper().getTypeFactory()
          .constructMapType(Map.class, String.class, String.class);
    }
    return type;
  }

  public boolean isDownloadFile() {
    return downloadFile;
  }

  private boolean checkDownloadFileFlag() {
    // todo: logic of find
    Response response = operationMeta.getSwaggerOperation().getResponses().get("200");
    if (response != null) {
      Model model = response.getResponseSchema();
      return model instanceof ModelImpl &&
          FileProperty.isType(((ModelImpl) model).getType(), ((ModelImpl) model).getFormat());
    }

    return false;
  }

  public boolean isFormData() {
    return formData;
  }

  public void setOperationMeta(OperationMeta operationMeta) {
    this.operationMeta = operationMeta;
  }

  // 输出b/c/形式的url
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

  // 为operation创建支持的多种produce processor
  protected void createProduceProcessors() {
    if (null == produces || produces.isEmpty()) {
      for (ProduceProcessor processor : ProduceProcessorManager.INSTANCE.values()) {
        this.produceProcessorMap.put(processor.getName(), processor);
      }
    } else {
      for (String produce : produces) {
        if (produce.contains(";")) {
          produce = produce.substring(0, produce.indexOf(";"));
        }
        ProduceProcessor processor = ProduceProcessorManager.INSTANCE.findValue(produce);
        if (processor == null) {
          LOGGER.error("produce {} is not supported", produce);
          continue;
        }
        this.produceProcessorMap.put(produce, processor);
      }

      if (produceProcessorMap.isEmpty()) {
        produceProcessorMap.put(ProduceProcessorManager.DEFAULT_TYPE, ProduceProcessorManager.DEFAULT_PROCESSOR);
      }
    }

    defaultProcessor = produceProcessorMap.values().stream().findFirst().get();
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
