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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoConst;
import org.apache.servicecomb.foundation.protobuf.internal.parser.ProtoParser;

import com.google.common.hash.Hashing;

import io.protostuff.compiler.model.Message;
import io.protostuff.compiler.model.Proto;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.vertx.core.json.Json;

public class SwaggerToProtoGenerator {
  private final String protoPackage;

  private final Swagger swagger;

  private final StringBuilder msgStringBuilder = new StringBuilder();

  private final StringBuilder serviceBuilder = new StringBuilder();

  private final Set<String> imports = new HashSet<>();

  private final Set<String> messages = new HashSet<>();

  private final List<Runnable> pending = new ArrayList<>();

  // not java package
  // better to be: app_${app}.mid_{microservice}.sid_{schemaId}
  public SwaggerToProtoGenerator(String protoPackage, Swagger swagger) {
    this.protoPackage = protoPackage;
    this.swagger = swagger;
  }

  public Proto convert() {
    convertDefinitions();
    convertOperations();
    for (Runnable runnable : pending) {
      runnable.run();
    }

    return createProto();
  }

  private void convertDefinitions() {
    if (swagger.getDefinitions() == null) {
      return;
    }

    for (Entry<String, Model> entry : swagger.getDefinitions().entrySet()) {
      convertDefinition(entry.getKey(), (ModelImpl) entry.getValue());
    }
  }

  private void convertDefinition(String modelName, ModelImpl model) {
    Map<String, Property> properties = model.getProperties();
    if (properties == null) {
      // it's a empty message
      properties = Collections.emptyMap();
    }

    // complex
    messages.add(modelName);
    appendLine(msgStringBuilder, "message %s {", modelName);
    int tag = 1;
    for (Entry<String, Property> entry : properties.entrySet()) {
      Property property = entry.getValue();
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
      return type;
    }

    Property property = adapter.getArrayItem();
    if (property != null) {
      return "repeated " + convertSwaggerType(property);
    }

    property = adapter.getMapItem();
    if (property != null) {
      return String.format("map<string, %s>", convertSwaggerType(property));
    }

    if (adapter.isObject()) {
      addImports(ProtoConst.ANY_PROTO);
      return ProtoConst.ANY.getCanonicalName();
    }

    throw new IllegalStateException(String
        .format("not support swagger type, class=%s, content=%s.", swaggerType.getClass().getName(),
            Json.encode(swaggerType)));
  }

  private String tryFindEnumType(List<String> enums) {
    if (enums != null && !enums.isEmpty()) {
      String strEnums = enums.toString();
      String enumName = "Enum_" + Hashing.sha256().hashString(strEnums, StandardCharsets.UTF_8).toString();
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
      appendLine(msgStringBuilder, "  %s =%d;", enums.get(idx), idx);
    }
    appendLine(msgStringBuilder, "}");
  }

  private String findBaseType(String swaggerType, String swaggerFmt) {
    String key = swaggerType + ":" + swaggerFmt;
    switch (key) {
      case "boolean:null":
        return "bool";
      // there is no int8/int16 in protobuf
      case "integer:null":
        return "int64";
      case "integer:int8":
      case "integer:int16":
      case "integer:int32":
        return "int32";
      case "integer:int64":
        return "int64";
      case "number:null":
        return "double";
      case "number:float":
        return "float";
      case "number:double":
        return "double";
      case "string:null":
        return "string";
      case "string:byte":
        return "bytes";
      case "string:date": // LocalDate
      case "string:date-time": // Date
        return "int64";
      case "file:null":
        throw new IllegalStateException("not support swagger type: " + swaggerType);
    }
    return null;
  }

  private void convertOperations() {
    Map<String, Path> paths = swagger.getPaths();
    if (paths == null || paths.isEmpty()) {
      return;
    }

    appendLine(serviceBuilder, "service MainService {");
    for (Path path : paths.values()) {
      for (Operation operation : path.getOperationMap().values()) {
        convertOpeation(operation);
      }
    }
    serviceBuilder.setLength(serviceBuilder.length() - 1);
    appendLine(serviceBuilder, "}");
  }

  private void convertOpeation(Operation operation) {
    ProtoMethod protoMethod = new ProtoMethod();
    fillRequestType(operation, protoMethod);
    fillResponseType(operation, protoMethod);

    appendLine(serviceBuilder, "  //%s%s", ProtoConst.OP_HINT, Json.encode(protoMethod));
    appendLine(serviceBuilder, "  rpc %s (%s) returns (%s);\n", operation.getOperationId(),
        protoMethod.getArgTypeName(),
        protoMethod.findResponse(Status.OK.getStatusCode()).getTypeName());
  }

  private void fillRequestType(Operation operation, ProtoMethod protoMethod) {
    List<Parameter> parameters = operation.getParameters();
    if (parameters.isEmpty()) {
      addImports(ProtoConst.EMPTY_PROTO);
      protoMethod.setArgTypeName(ProtoConst.EMPTY.getCanonicalName());
      return;
    }

    if (parameters.size() == 1) {
      String type = convertSwaggerType(parameters.get(0));
      if (messages.contains(type)) {
        protoMethod.setArgTypeName(type);
        return;
      }
    }

    String wrapName = operation.getOperationId() + "RequestWrap";
    createWrapArgs(wrapName, parameters);

    protoMethod.setArgWrapped(true);
    protoMethod.setArgTypeName(wrapName);
  }

  private void fillResponseType(Operation operation, ProtoMethod protoMethod) {
    for (Entry<String, Response> entry : operation.getResponses().entrySet()) {
      String type = convertSwaggerType(entry.getValue().getSchema());

      ProtoResponse protoResponse = new ProtoResponse();
      protoResponse.setWrapped(!messages.contains(type));
      protoResponse.setTypeName(type);

      if (protoResponse.isWrapped()) {
        String wrapName = operation.getOperationId() + "ResponseWrap" + entry.getKey();
        appendLine(msgStringBuilder, "message %s {", wrapName);
        appendLine(msgStringBuilder, "  %s response = 1;", type);
        appendLine(msgStringBuilder, "}");

        protoResponse.setTypeName(wrapName);
      }
      protoMethod.addResponse(entry.getKey(), protoResponse);
    }
  }

  private void createWrapArgs(String wrapName, List<Parameter> parameters) {
    appendLine(msgStringBuilder, "message %s {", wrapName);

    int idx = 1;
    for (Parameter parameter : parameters) {
      String type = convertSwaggerType(parameter);
      appendLine(msgStringBuilder, "  %s %s = %d;", type, parameter.getName(), idx);
      idx++;
    }

    appendLine(msgStringBuilder, "}");
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
