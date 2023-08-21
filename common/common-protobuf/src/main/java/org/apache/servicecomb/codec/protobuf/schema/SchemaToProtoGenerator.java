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
package org.apache.servicecomb.codec.protobuf.schema;


import static org.apache.servicecomb.foundation.common.utils.StringBuilderUtils.appendLine;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoConst;
import org.apache.servicecomb.foundation.protobuf.internal.parser.ProtoParser;
import org.springframework.util.CollectionUtils;

import com.google.common.hash.Hashing;

import io.protostuff.compiler.model.Message;
import io.protostuff.compiler.model.Proto;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SchemaToProtoGenerator {
  private final String protoPackage;

  private final OpenAPI openAPI;

  private final Schema<?> rootSchema;

  private final String rootName;

  private final Set<String> imports = new HashSet<>();

  private final Set<String> messages = new HashSet<>();

  private final StringBuilder msgStringBuilder = new StringBuilder();

  private List<Runnable> pending = new ArrayList<>();

  public SchemaToProtoGenerator(String protoPackage, OpenAPI openAPI, Schema<?> rootSchema, String rootName) {
    this.protoPackage = protoPackage;
    this.openAPI = openAPI;
    this.rootSchema = rootSchema;
    this.rootName = rootName;
  }

  public Proto convert() {
    convertSwaggerType(this.rootSchema);

    Map<String, Schema> wrap = new HashMap<>(1);
    wrap.put("value", this.rootSchema);
    createMessage(rootName, wrap, ProtoConst.ANNOTATION_WRAP_PROPERTY);

    do {
      List<Runnable> oldPending = pending;
      pending = new ArrayList<>();
      for (Runnable runnable : oldPending) {
        runnable.run();
      }
    } while (!pending.isEmpty());

    return createProto();
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
    ProtoParser protoParser = new ProtoParser();
    return protoParser.parseFromContent(sb.toString());
  }

  private String convertSwaggerType(Schema<?> swaggerType) {
    @SuppressWarnings("unchecked")
    String type = tryFindEnumType((List<String>) swaggerType.getEnum());
    if (type != null) {
      return type;
    }

    type = findBaseType(swaggerType.getType(), swaggerType.getFormat());
    if (type != null) {
      return type;
    }

    Schema<?> itemProperty = swaggerType.getItems();
    if (itemProperty != null) {
      return "repeated " + convertArrayOrMapItem(itemProperty);
    }

    itemProperty = swaggerType.getAdditionalItems();
    if (itemProperty != null) {
      return String.format("map<string, %s>", convertArrayOrMapItem(itemProperty));
    }

    type = swaggerType.get$ref();
    if (type != null) {
      Schema<?> refSchema = openAPI.getComponents().getSchemas().get(
          type.substring(Components.COMPONENTS_SCHEMAS_REF.length()));
      if (refSchema == null) {
        throw new IllegalArgumentException("not found ref in components " + type);
      }
      return convertSwaggerType(refSchema);
    }

    Map<String, Schema> properties = swaggerType.getProperties();
    if (CollectionUtils.isEmpty(properties)) {
      addImports(ProtoConst.ANY_PROTO);
      return ProtoConst.ANY.getCanonicalName();
    }
    createMessage(swaggerType.getName(), properties);
    return swaggerType.getName();
  }

  private void addImports(Proto proto) {
    imports.add(proto.getFilename());
    for (Message message : proto.getMessages()) {
      messages.add(message.getCanonicalName());
    }
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

  public static boolean isValidEnum(String name) {
    return !name.contains(".") && !name.contains("-");
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

  private String findBaseType(String swaggerType, String swaggerFmt) {
    String key = swaggerType + ":" + swaggerFmt;
    return switch (key) {
      case "boolean:null" -> "bool";
      case "integer:null", "integer:int64" -> "int64";
      case "integer:int32" -> "int32";
      case "number:null", "number:double" -> "double";
      case "number:float" -> "float";
      case "string:null" -> "string";
      case "string:byte" -> "bytes";
      case "string:date", "string:date-time" -> "int64";
      case "string:binary" -> throw new IllegalArgumentException("proto buffer not support file upload/download");
      default -> null;
    };
  }

  private String convertArrayOrMapItem(Schema<?> itemProperty) {
    // List<List<>>, need to wrap
    if (itemProperty.getItems() != null) {
      String protoName = generateWrapPropertyName(List.class.getSimpleName(), itemProperty.getItems());
      pending.add(() -> wrapPropertyToMessage(protoName, itemProperty));
      return protoName;
    }

    // List<Map<>>, need to wrap
    if (itemProperty.getAdditionalItems() != null) {
      String protoName = generateWrapPropertyName(Map.class.getSimpleName(), itemProperty.getAdditionalItems());
      pending.add(() -> wrapPropertyToMessage(protoName, itemProperty));
      return protoName;
    }

    return convertSwaggerType(itemProperty);
  }


  private String generateWrapPropertyName(String prefix, Schema<?> property) {
    // List<List<>>, need to wrap
    if (property.getItems() != null) {
      return generateWrapPropertyName(prefix + List.class.getSimpleName(), property.getItems());
    }

    // List<Map<>>, need to wrap
    if (property.getAdditionalItems() != null) {
      return generateWrapPropertyName(prefix + Map.class.getSimpleName(), property.getAdditionalItems());
    }

    // message name cannot have . (package separator)
    return prefix + StringUtils.capitalize(escapeMessageName(convertSwaggerType(property)));
  }

  public static String escapeMessageName(String name) {
    return name.replaceAll("\\.", "_");
  }


  private void wrapPropertyToMessage(String protoName, Schema<?> property) {
    createMessage(protoName, Collections.singletonMap("value", property), ProtoConst.ANNOTATION_WRAP_PROPERTY);
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
}
