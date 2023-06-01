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

package org.apache.servicecomb.swagger;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.servicecomb.foundation.common.base.DynamicEnum;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.swagger.extend.PropertyModelConverterExt;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.Status.Family;

public final class SwaggerUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerUtils.class);


  private SwaggerUtils() {
  }

  public static String swaggerToString(OpenAPI swagger) {
    try {
      return Yaml.mapper().writeValueAsString(swagger);
    } catch (Throwable e) {
      throw new ServiceCombException("Convert swagger to string failed, ", e);
    }
  }

  public static OpenAPI parseSwagger(URL url) {
    try {
      String swaggerContent = IOUtils.toString(url, StandardCharsets.UTF_8);
      return internalParseSwagger(swaggerContent);
    } catch (Throwable e) {
      throw new ServiceCombException("Parse swagger from url failed, url=" + url, e);
    }
  }

  public static OpenAPI parseAndValidateSwagger(URL url) {
    OpenAPI swagger = SwaggerUtils.parseSwagger(url);
    SwaggerUtils.validateSwagger(swagger);
    return swagger;
  }

  public static OpenAPI parseSwagger(String swaggerContent) {
    try {
      return internalParseSwagger(swaggerContent);
    } catch (Throwable e) {
      throw new ServiceCombException("Parse swagger from content failed, ", e);
    }
  }

  public static OpenAPI parseAndValidateSwagger(String swaggerContent) {
    OpenAPI swagger = SwaggerUtils.parseSwagger(swaggerContent);
    SwaggerUtils.validateSwagger(swagger);
    return swagger;
  }

  /**
   * Provide a method to validate swagger. This method is now implemented to check common errors, and the logic
   * will be changed when necessary. For internal use only.
   */
  public static void validateSwagger(OpenAPI swagger) {
    Paths paths = swagger.getPaths();
    if (paths == null) {
      return;
    }

    for (PathItem path : paths.values()) {
      Operation operation = path.getPost();
      if (operation == null) {
        continue;
      }

      for (Parameter parameter : operation.getParameters()) {
        if (BodyParameter.class.isInstance(parameter) &&
            ((BodyParameter) parameter).getSchema() == null) {
          throw new ServiceCombException("swagger validator: body parameter schema is empty.");
        }
      }
    }
  }

  private static OpenAPI internalParseSwagger(String swaggerContent) throws IOException {
    OpenAPI swagger = Yaml.mapper().readValue(swaggerContent, OpenAPI.class);
    correctResponses(swagger);
    return swagger;
  }

  public static void correctResponses(Operation operation) {
    int okCode = Status.OK.getStatusCode();
    String strOkCode = String.valueOf(okCode);
    ApiResponse okResponse = null;

    for (Entry<String, ApiResponse> responseEntry : operation.getResponses().entrySet()) {
      ApiResponse response = responseEntry.getValue();
      if (StringUtils.isEmpty(response.getDescription())) {
        response.setDescription("response of " + responseEntry.getKey());
      }

      if (operation.getResponses().get(strOkCode) != null) {
        continue;
      }

      int statusCode = NumberUtils.toInt(responseEntry.getKey());
      if ("default".equals(responseEntry.getKey())) {
        statusCode = okCode;
      }
      if (Family.SUCCESSFUL.equals(Family.familyOf(statusCode))) {
        okResponse = response;
      }
    }

    if (okResponse != null) {
      operation.getResponses().addApiResponse(strOkCode, okResponse);
    }
  }

  public static void correctResponses(OpenAPI swagger) {
    if (swagger.getPaths() == null) {
      return;
    }

    for (PathItem path : swagger.getPaths().values()) {
      for (Operation operation : path.readOperations()) {
        correctResponses(operation);
      }
    }
  }

  public static Map<String, Property> getBodyProperties(OpenAPI swagger, Parameter parameter) {
    if (!(parameter instanceof BodyParameter)) {
      return null;
    }

    Model model = ((BodyParameter) parameter).getSchema();
    if (model instanceof RefModel) {
      model = swagger.getDefinitions().get(((RefModel) model).getSimpleRef());
    }

    if (model instanceof ModelImpl) {
      return model.getProperties();
    }

    return null;
  }

  public static void addDefinitions(OpenAPI swagger, Type paramType) {
    JavaType javaType = TypeFactory.defaultInstance().constructType(paramType);
    if (javaType.isTypeOrSubTypeOf(DynamicEnum.class)) {
      return;
    }
    Map<String, Schema> models = ModelConverters.getInstance().readAll(javaType);
    for (Entry<String, Schema> entry : models.entrySet()) {
      if (!modelNotDuplicate(swagger, entry)) {
        LOGGER.warn("duplicate param model: " + entry.getKey());
        throw new IllegalArgumentException("duplicate param model: " + entry.getKey());
      }
    }
  }

  private static boolean modelNotDuplicate(OpenAPI swagger, Entry<String, Schema> entry) {
    if (null == swagger.) {
      swagger.addDefinition(entry.getKey(), entry.getValue());
      return true;
    }
    Model tempModel = swagger.getDefinitions().get(entry.getKey());
    if (null != tempModel && !tempModel.equals(entry.getValue())) {
      if (modelOfClassNotDuplicate(tempModel, entry.getValue())) {
        swagger.addDefinition(entry.getKey(), tempModel);
        return true;
      } else {
        return false;
      }
    }
    swagger.addDefinition(entry.getKey(), entry.getValue());
    return true;
  }

  private static boolean modelOfClassNotDuplicate(OpenAPI tempModel, Schema model) {
    String tempModelClass = (String) tempModel.getVendorExtensions().get(SwaggerConst.EXT_JAVA_CLASS);
    String modelClass = (String) model.getVendorExtensions().get(SwaggerConst.EXT_JAVA_CLASS);
    return tempModelClass.equals(modelClass);
  }

  public static void setParameterType(OpenAPI swagger, JavaType type, AbstractSerializableParameter<?> parameter) {
    addDefinitions(swagger, type);
    Property property = ModelConverters.getInstance().readAsProperty(type);

    if (isComplexProperty(property)) {
      // cannot set a simple parameter(header, query, etc.) as complex type
      String msg = String
          .format("not allow complex type for %s parameter, type=%s.", parameter.getIn(), type.toCanonical());
      throw new IllegalStateException(msg);
    }
    parameter.setProperty(property);
  }

  public static boolean isBean(Schema model) {
    return isBean(PropertyModelConverterExt.toProperty(model));
  }

  public static boolean isBean(Property property) {
    return property instanceof RefProperty || property instanceof ObjectProperty;
  }

  public static boolean isComplexProperty(Property property) {
    if (property instanceof RefProperty || property instanceof ObjectProperty || property instanceof MapProperty) {
      return true;
    }

    if (ArrayProperty.class.isInstance(property)) {
      return isComplexProperty(((ArrayProperty) property).getItems());
    }

    return false;
  }

  public static ModelImpl getModelImpl(OpenAPI swagger, BodyParameter bodyParameter) {
    Model model = bodyParameter.getSchema();
    if (model instanceof ModelImpl) {
      return (ModelImpl) model;
    }

    if (!(model instanceof RefModel)) {
      return null;
    }

    String simpleRef = ((RefModel) model).getSimpleRef();
    Model targetModel = swagger.getDefinitions().get(simpleRef);
    return targetModel instanceof ModelImpl ? (ModelImpl) targetModel : null;
  }

  public static void setCommaConsumes(Swagger swagger, String commaConsumes) {
    if (StringUtils.isEmpty(commaConsumes)) {
      return;
    }

    setConsumes(swagger, commaConsumes.split(","));
  }

  public static void setCommaConsumes(Operation operation, String commaConsumes) {
    if (StringUtils.isEmpty(commaConsumes)) {
      return;
    }

    setConsumes(operation, commaConsumes.split(","));
  }

  public static void setConsumes(Operation operation, String... consumes) {
    List<String> consumeList = convertConsumesOrProduces(consumes);
    if (!consumeList.isEmpty()) {
      operation.setConsumes(consumeList);
    }
  }

  public static void setConsumes(Swagger swagger, String... consumes) {
    List<String> consumeList = convertConsumesOrProduces(consumes);
    if (!consumeList.isEmpty()) {
      swagger.setConsumes(consumeList);
    }
  }

  public static List<String> convertConsumesOrProduces(String... consumesOrProduces) {
    return Arrays.stream(consumesOrProduces)
        .map(String::trim)
        .filter(StringUtils::isNotEmpty)
        .collect(Collectors.toList());
  }

  public static void setCommaProduces(Swagger swagger, String commaProduces) {
    if (StringUtils.isEmpty(commaProduces)) {
      return;
    }

    setProduces(swagger, commaProduces.split(","));
  }

  public static void setCommaProduces(Operation operation, String commaProduces) {
    if (StringUtils.isEmpty(commaProduces)) {
      return;
    }

    setProduces(operation, commaProduces.split(","));
  }

  public static void setProduces(Operation operation, String... produces) {
    List<String> produceList = convertConsumesOrProduces(produces);
    if (!produceList.isEmpty()) {
      operation.setProduces(produceList);
    }
  }

  public static void setProduces(Swagger swagger, String... produces) {
    List<String> produceList = convertConsumesOrProduces(produces);
    if (!produceList.isEmpty()) {
      swagger.setProduces(produceList);
    }
  }

  public static boolean hasAnnotation(Class<?> cls, Class<? extends Annotation> annotation) {
    if (cls.getAnnotation(annotation) != null) {
      return true;
    }

    for (Method method : cls.getMethods()) {
      if (method.getAnnotation(annotation) != null) {
        return true;
      }
    }

    return false;
  }

  public static boolean isRawJsonType(Parameter param) {
    Object rawJson = param.getVendorExtensions().get(SwaggerConst.EXT_RAW_JSON_TYPE);
    if (rawJson instanceof Boolean) {
      return (boolean) rawJson;
    }
    return false;
  }

  public static Class<?> getInterface(OpenAPI swagger) {
    Info info = swagger.getInfo();
    if (info == null) {
      return null;
    }

    String name = getInterfaceName(info.getVendorExtensions());
    if (StringUtils.isEmpty(name)) {
      return null;
    }

    return ReflectUtils.getClassByName(name);
  }

  public static String getClassName(Map<String, Object> vendorExtensions) {
    return getVendorExtension(vendorExtensions, SwaggerConst.EXT_JAVA_CLASS);
  }

  public static String getInterfaceName(Map<String, Object> vendorExtensions) {
    return getVendorExtension(vendorExtensions, SwaggerConst.EXT_JAVA_INTF);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getVendorExtension(Map<String, Object> vendorExtensions, String key) {
    if (vendorExtensions == null) {
      return null;
    }

    return (T) vendorExtensions.get(key);
  }

  public static boolean isBean(Type type) {
    if (type == null) {
      return false;
    }

    JavaType javaType = TypeFactory.defaultInstance().constructType(type);
    if (javaType.isContainerType() || javaType.isEnumType() || javaType.isTypeOrSubTypeOf(DynamicEnum.class)) {
      return false;
    }

    Class<?> cls = javaType.getRawClass();
    if (ClassUtils.isPrimitiveOrWrapper(cls)) {
      return false;
    }

    return (cls != String.class
        && cls != Date.class
        && cls != LocalDate.class
        && cls != byte[].class
        && cls != File.class
        && !cls.getName().equals("org.springframework.web.multipart.MultipartFile")
        && !Part.class.isAssignableFrom(cls));
  }

  public static boolean isFileParameter(Parameter parameter) {
    if (!(parameter instanceof FormParameter)) {
      return false;
    }

    FormParameter formParameter = (FormParameter) parameter;
    if (FileProperty.isType(formParameter.getType(), formParameter.getFormat())) {
      return true;
    }

    Property property = formParameter.getItems();
    if (!ArrayProperty.isType(formParameter.getType()) || property == null) {
      return false;
    }

    return FileProperty.isType(property.getType(), property.getFormat());
  }
}
