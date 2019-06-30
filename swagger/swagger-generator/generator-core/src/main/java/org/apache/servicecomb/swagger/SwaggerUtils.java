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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.swagger.generator.SwaggerConst;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.converter.ModelConverters;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.FileProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.utils.PropertyModelConverter;
import io.swagger.util.Yaml;

public final class SwaggerUtils {
  private SwaggerUtils() {
  }

  public static String swaggerToString(Swagger swagger) {
    try {
      return Yaml.mapper().writeValueAsString(swagger);
    } catch (Throwable e) {
      throw new ServiceCombException("Convert swagger to string failed, ", e);
    }
  }

  public static Swagger parseSwagger(URL url) {
    try {
      String swaggerContent = IOUtils.toString(url, StandardCharsets.UTF_8);
      return internalParseSwagger(swaggerContent);
    } catch (Throwable e) {
      throw new ServiceCombException("Parse swagger from url failed, url=" + url, e);
    }
  }

  public static Swagger parseAndValidateSwagger(URL url) {
    Swagger swagger = SwaggerUtils.parseSwagger(url);
    SwaggerUtils.validateSwagger(swagger);
    return swagger;
  }

  public static Swagger parseSwagger(String swaggerContent) {
    try {
      return internalParseSwagger(swaggerContent);
    } catch (Throwable e) {
      throw new ServiceCombException("Parse swagger from content failed, ", e);
    }
  }

  public static Swagger parseAndValidateSwagger(String swaggerContent) {
    Swagger swagger = SwaggerUtils.parseSwagger(swaggerContent);
    SwaggerUtils.validateSwagger(swagger);
    return swagger;
  }

  /**
   * Provide a method to validate swagger. This method is now implemented to check common errors, and the logic
   * will be changed when necessary. For internal use only.
   */
  public static void validateSwagger(Swagger swagger) {
    Map<String, Path> paths = swagger.getPaths();
    if (paths == null) {
      return;
    }

    for (Path path : paths.values()) {
      Operation operation = path.getPost();
      if (operation == null) {
        continue;
      }

      for (Parameter parameter : operation.getParameters()) {
        if (BodyParameter.class.isInstance(parameter) &&
            !isRawJsonType(parameter) &&
            ((BodyParameter) parameter).getSchema() == null) {
          throw new ServiceCombException("swagger validator: body parameter schema is empty.");
        }
      }
    }
  }

  private static Swagger internalParseSwagger(String swaggerContent) throws IOException {
    Swagger swagger = Yaml.mapper().readValue(swaggerContent, Swagger.class);
    correctResponses(swagger);
    return swagger;
  }

  public static void correctResponses(Operation operation) {
    int okCode = Status.OK.getStatusCode();
    String strOkCode = String.valueOf(okCode);
    Response okResponse = null;

    for (Entry<String, Response> responseEntry : operation.getResponses().entrySet()) {
      Response response = responseEntry.getValue();
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
      operation.addResponse(strOkCode, okResponse);
    }
  }

  public static void correctResponses(Swagger swagger) {
    if (swagger.getPaths() == null) {
      return;
    }

    for (Path path : swagger.getPaths().values()) {
      for (Operation operation : path.getOperations()) {
        correctResponses(operation);
      }
    }
  }

  public static Map<String, Property> getBodyProperties(Swagger swagger, Parameter parameter) {
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

  public static void addDefinitions(Swagger swagger, Type paramType) {
    Map<String, Model> models = ModelConverters.getInstance().readAll(paramType);
    for (Entry<String, Model> entry : models.entrySet()) {
      swagger.addDefinition(entry.getKey(), entry.getValue());
    }
  }

  public static void setParameterType(Swagger swagger, Type type, AbstractSerializableParameter<?> parameter) {
    addDefinitions(swagger, type);
    Property property = ModelConverters.getInstance().readAsProperty(type);

    if (isComplexProperty(property)) {
      // cannot set a simple parameter(header, query, etc.) as complex type
      String msg = String
          .format("not allow complex type for %s parameter, type=%s.", parameter.getIn(), type.getTypeName());
      throw new IllegalStateException(msg);
    }
    parameter.setProperty(property);
  }

  public static boolean isBean(Model model) {
    return isBean(new PropertyModelConverter().modelToProperty(model));
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

  public static ModelImpl getModelImpl(Swagger swagger, BodyParameter bodyParameter) {
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

  public static Class<?> getInterface(Swagger swagger) {
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
    if (javaType.isContainerType()) {
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
