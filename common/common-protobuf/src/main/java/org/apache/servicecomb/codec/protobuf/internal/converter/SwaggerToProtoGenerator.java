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
package org.apache.servicecomb.codec.protobuf.internal.converter;

import static org.apache.servicecomb.foundation.common.utils.StringBuilderUtils.appendLine;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoConst;
import org.apache.servicecomb.foundation.protobuf.internal.parser.ProtoParser;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;

import io.protostuff.compiler.model.Message;
import io.protostuff.compiler.model.Proto;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.vertx.core.json.Json;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SwaggerToProtoGenerator {
  private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerToProtoGenerator.class);

  private final String protoPackage;

  private final OpenAPI swagger;

  private final StringBuilder msgStringBuilder = new StringBuilder();

  private final StringBuilder serviceBuilder = new StringBuilder();

  private final Set<String> imports = new HashSet<>();

  private final Set<String> messages = new HashSet<>();

  private List<Runnable> pending = new ArrayList<>();

  // not java package
  // better to be: app_${app}.mid_{microservice}.sid_{schemaId}
  public SwaggerToProtoGenerator(String protoPackage, OpenAPI swagger) {
    this.protoPackage = escapePackageName(protoPackage);
    this.swagger = swagger;
  }

  public Proto convert() {
    convertDefinitions();
    convertOperations();
    for (; ; ) {
      List<Runnable> oldPending = pending;
      pending = new ArrayList<>();
      for (Runnable runnable : oldPending) {
        runnable.run();
      }
      if (pending.isEmpty()) {
        break;
      }
    }

    return createProto();
  }

  public static String escapePackageName(String name) {
    return name.replaceAll("[\\-\\:]", "_");
  }

  public static String escapeMessageName(String name) {
    return name.replaceAll("\\.", "_");
  }

  public static boolean isValidEnum(String name) {
    if (name.contains(".") || name.contains("-")) {
      return false;
    }
    return true;
  }

  private void convertDefinitions() {
    if (swagger.getComponents() == null || swagger.getComponents().getSchemas() == null) {
      return;
    }

    for (Entry<String, Schema> entry : swagger.getComponents().getSchemas().entrySet()) {
      convertDefinition(entry.getKey(), entry.getValue());
    }
  }

  @SuppressWarnings("unchecked")
  private void convertDefinition(String modelName, Schema model) {
    Map<String, Schema> properties = model.getProperties();
    if (properties == null) {
      // it's a empty message
      properties = Collections.emptyMap();
    }

    createMessage(modelName, properties);
  }

  private void createMessage(String protoName, Map<String, Schema> properties, String... annotations) {
    if (!messages.add(protoName)) {
      // already created
      return;
    }

    for (String annotation : annotations) {
      msgStringBuilder.append("//");
      appendLine(msgStringBuilder, annotation);
    }
    appendLine(msgStringBuilder, "message %s {", protoName);
    int tag = 1;
    for (Entry<String, Schema> entry : properties.entrySet()) {
      Schema property = entry.getValue();
      String propertyType = convertSwaggerType(property);

      appendLine(msgStringBuilder, "  %s %s = %d;", propertyType, entry.getKey(), tag);
      tag++;
    }
    appendLine(msgStringBuilder, "}");
  }

  private void addImports(Proto proto) {
    imports.add(proto.getFilename());
    for (Message message : proto.getMessages()) {
      messages.add(message.getCanonicalName());
    }
  }

  private String convertSwaggerType(Object swaggerType) {
    if (swaggerType == null) {
      // void
      addImports(ProtoConst.EMPTY_PROTO);
      return ProtoConst.EMPTY.getCanonicalName();
    }

    SwaggerTypeAdapter adapter = SwaggerTypeAdapter.create(swaggerType);
    String type = tryFindEnumType(adapter.getEnum());
    if (type != null) {
      return type;
    }

    type = findBaseType(adapter.getType(), adapter.getFormat());
    if (type != null) {
      return type;
    }

    type = adapter.getRefType();
    if (type != null) {
      return type.substring(Components.COMPONENTS_SCHEMAS_REF.length());
    }

    Schema itemProperty = adapter.getArrayItem();
    if (itemProperty != null) {
      return "repeated " + convertArrayOrMapItem(itemProperty);
    }

    itemProperty = adapter.getMapItem();
    if (itemProperty != null) {
      return String.format("map<string, %s>", convertArrayOrMapItem(itemProperty));
    }

    if (adapter.isJavaLangObject()) {
      addImports(ProtoConst.ANY_PROTO);
      return ProtoConst.ANY.getCanonicalName();
    }

    throw new IllegalStateException(String
        .format("not support swagger type, class=%s, content=%s.", swaggerType.getClass().getName(),
            Json.encode(swaggerType)));
  }

  private String convertArrayOrMapItem(Schema itemProperty) {
    SwaggerTypeAdapter itemAdapter = SwaggerTypeAdapter.create(itemProperty);
    // List<List<>>, need to wrap
    if (itemAdapter.getArrayItem() != null) {
      String protoName = generateWrapPropertyName(List.class.getSimpleName(), itemAdapter.getArrayItem());
      pending.add(() -> wrapPropertyToMessage(protoName, itemProperty));
      return protoName;
    }

    // List<Map<>>, need to wrap
    if (itemAdapter.getMapItem() != null) {
      String protoName = generateWrapPropertyName(Map.class.getSimpleName(), itemAdapter.getMapItem());
      pending.add(() -> wrapPropertyToMessage(protoName, itemProperty));
      return protoName;
    }

    return convertSwaggerType(itemProperty);
  }

  private String generateWrapPropertyName(String prefix, Schema property) {
    SwaggerTypeAdapter adapter = SwaggerTypeAdapter.create(property);
    // List<List<>>, need to wrap
    if (adapter.getArrayItem() != null) {
      return generateWrapPropertyName(prefix + List.class.getSimpleName(), adapter.getArrayItem());
    }

    // List<Map<>>, need to wrap
    if (adapter.getMapItem() != null) {
      return generateWrapPropertyName(prefix + Map.class.getSimpleName(), adapter.getMapItem());
    }

    // message name cannot have . (package separator)
    return prefix + StringUtils.capitalize(escapeMessageName(convertSwaggerType(adapter)));
  }

  private void wrapPropertyToMessage(String protoName, Schema property) {
    createMessage(protoName, Collections.singletonMap("value", property), ProtoConst.ANNOTATION_WRAP_PROPERTY);
  }

  private String tryFindEnumType(List<String> enums) {
    if (enums != null && !enums.isEmpty()) {
      String strEnums = enums.toString();
      String enumName = "Enum_" + Hashing.sha256().hashString(strEnums, StandardCharsets.UTF_8);
      pending.add(() -> createEnum(enumName, enums));
      return enumName;
    }
    return null;
  }

  private void createEnum(String enumName, List<String> enums) {
    if (!messages.add(enumName)) {
      // already created
      return;
    }

    appendLine(msgStringBuilder, "enum %s {", enumName);
    for (int idx = 0; idx < enums.size(); idx++) {
      if (isValidEnum(enums.get(idx))) {
        appendLine(msgStringBuilder, "  %s =%d;", enums.get(idx), idx);
      } else {
        throw new IllegalStateException(
            String.format("enum class [%s] name [%s] not supported by protobuffer.", enumName, enums.get(idx)));
      }
    }
    appendLine(msgStringBuilder, "}");
  }

  private String findBaseType(String swaggerType, String swaggerFmt) {
    String key = swaggerType + ":" + swaggerFmt;
    return switch (key) {
      case "boolean:null" -> "bool";
      // there is no int8/int16 in protobuf
      case "integer:null" -> "int64";
      case "integer:int8", "integer:int16", "integer:int32" -> "int32";
      case "integer:int64" -> "int64";
      case "number:null" -> "double";
      case "number:float" -> "float";
      case "number:double" -> "double";
      case "string:null" -> "string";
      case "string:byte" -> "bytes"; // LocalDate
      case "string:date", "string:date-time" -> // Date
          "int64";
      case "file:null" -> throw new IllegalStateException("not support swagger type: " + swaggerType);
      default -> null;
    };
  }

  private void convertOperations() {
    Paths paths = swagger.getPaths();
    if (paths == null || paths.isEmpty()) {
      return;
    }

    appendLine(serviceBuilder, "service MainService {");
    for (PathItem path : paths.values()) {
      for (Operation operation : path.readOperations()) {
        if (isUpload(operation)) {
          LOGGER.warn("Not support operation for highway {}.{}, {}", this.protoPackage, operation.getOperationId(),
              "file upload not supported");
          continue;
        } else if (isDownload(operation)) {
          LOGGER.warn("Not support operation for highway {}.{}, {}", this.protoPackage, operation.getOperationId(),
              "file download not supported");
          continue;
        }
        try {
          convertOperation(operation);
        } catch (Exception e) {
          LOGGER.error("Not support operation for highway {}.{}", this.protoPackage, operation.getOperationId(), e);
        }
      }
    }

    serviceBuilder.setLength(serviceBuilder.length() - 1);

    appendLine(serviceBuilder, "}");
  }

  private boolean isUpload(Operation operation) {
    return operation.getRequestBody() != null && operation.getRequestBody().getContent() != null
        && operation.getRequestBody().getContent().get(MediaType.MULTIPART_FORM_DATA) != null;
  }

  private boolean isDownload(Operation operation) {
    return operation.getResponses().get(SwaggerConst.SUCCESS_KEY) != null &&
        operation.getResponses().get(SwaggerConst.SUCCESS_KEY).getContent() != null &&
        operation.getResponses().get(SwaggerConst.SUCCESS_KEY).getContent().get(MediaType.MULTIPART_FORM_DATA)
            != null;
  }

  private void convertOperation(Operation operation) {
    ProtoMethod protoMethod = new ProtoMethod();
    fillRequestType(operation, protoMethod);
    fillResponseType(operation, protoMethod);

    appendLine(serviceBuilder, "  //%s%s", ProtoConst.ANNOTATION_RPC, Json.encode(protoMethod));
    appendLine(serviceBuilder, "  rpc %s (%s) returns (%s);\n", operation.getOperationId(),
        protoMethod.getArgTypeName(),
        protoMethod.findResponse(Status.OK.getStatusCode()).getTypeName());
  }

  private void fillRequestType(Operation operation, ProtoMethod protoMethod) {
    int parametersCount = parametersCount(operation);
    if (parametersCount == 0) {
      addImports(ProtoConst.EMPTY_PROTO);
      protoMethod.setArgTypeName(ProtoConst.EMPTY.getCanonicalName());
      return;
    }

    if (parametersCount == 1) {
      String type = convertSwaggerType(oneSchema(operation));
      if (messages.contains(type)) {
        protoMethod.setArgTypeName(type);
        return;
      }
    }

    String wrapName = StringUtils.capitalize(operation.getOperationId()) + "RequestWrap";
    createWrapArgs(wrapName, wrapSchema(operation));

    protoMethod.setArgTypeName(wrapName);
  }

  private Map<String, Schema> wrapSchema(Operation operation) {
    Map<String, Schema> properties = new LinkedHashMap<>();
    if (operation.getParameters() != null) {
      for (Parameter parameter : operation.getParameters()) {
        properties.put(parameter.getName(), parameter.getSchema());
      }
    }
    if (operation.getRequestBody() != null
        && operation.getRequestBody().getContent().size() != 0) {
      if (operation.getRequestBody().getContent().get(SwaggerConst.FORM_MEDIA_TYPE) != null) {
        operation.getRequestBody().getContent().get(SwaggerConst.FORM_MEDIA_TYPE).getSchema().getProperties()
            .forEach((k, v) -> properties.put((String) k, (Schema) v));
      } else {
        properties.put((String) operation.getRequestBody().getExtensions().get(SwaggerConst.EXT_BODY_NAME),
            operation.getRequestBody().getContent().get(
                operation.getRequestBody().getContent().keySet().iterator().next()).getSchema());
      }
    }
    return properties;
  }

  private Schema oneSchema(Operation operation) {
    if (operation.getParameters() != null && operation.getParameters().size() == 1) {
      return operation.getParameters().get(0).getSchema();
    }
    if (operation.getRequestBody().getContent().get(SwaggerConst.FORM_MEDIA_TYPE) != null) {
      return (Schema) operation.getRequestBody().getContent().get(SwaggerConst.FORM_MEDIA_TYPE).getSchema()
          .getProperties()
          .values().iterator().next();
    }
    return operation.getRequestBody().getContent().get(
        operation.getRequestBody().getContent().keySet().iterator().next()).getSchema();
  }

  private int parametersCount(Operation operation) {
    int parameters = operation.getParameters() == null ? 0 : operation.getParameters().size();
    if (operation.getRequestBody() != null) {
      if (operation.getRequestBody().getContent().get(SwaggerConst.FORM_MEDIA_TYPE) != null) {
        parameters = parameters + operation.getRequestBody()
            .getContent().get(SwaggerConst.FORM_MEDIA_TYPE).getSchema().getProperties().size();
      } else if (operation.getRequestBody().getContent().size() != 0) {
        parameters = parameters + 1;
      }
    }
    return parameters;
  }

  private void fillResponseType(Operation operation, ProtoMethod protoMethod) {
    for (Entry<String, ApiResponse> entry : operation.getResponses().entrySet()) {
      Schema schema = null;
      if (entry.getValue().getContent() != null &&
          entry.getValue().getContent().size() != 0) {
        schema = entry.getValue().getContent().get(
            entry.getValue().getContent().keySet().iterator().next()).getSchema();
      }
      String type = convertSwaggerType(schema);
      boolean wrapped = !messages.contains(type);

      ProtoResponse protoResponse = new ProtoResponse();
      protoResponse.setTypeName(type);

      if (wrapped) {
        String wrapName = StringUtils.capitalize(operation.getOperationId()) + "ResponseWrap" + entry.getKey();
        wrapPropertyToMessage(wrapName, schema);

        protoResponse.setTypeName(wrapName);
      }
      protoMethod.addResponse(entry.getKey(), protoResponse);
    }
  }

  private void createWrapArgs(String wrapName, Map<String, Schema> properties) {
    createMessage(wrapName, properties, ProtoConst.ANNOTATION_WRAP_ARGUMENTS);
  }

  protected Proto createProto() {
    StringBuilder sb = new StringBuilder();
    appendLine(sb, "syntax = \"proto3\";");
    for (String importMsg : imports) {
      appendLine(sb, "import \"%s\";", importMsg);
    }
    if (StringUtils.isNotEmpty(protoPackage)) {
      sb.append("package ").append(protoPackage).append(";\n");
    }
    sb.append(msgStringBuilder);
    sb.append(serviceBuilder);

    ProtoParser protoParser = new ProtoParser();
    return protoParser.parseFromContent(sb.toString());
  }
}
