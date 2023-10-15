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
import org.apache.servicecomb.swagger.SwaggerUtils;

import com.google.common.hash.Hashing;

import io.protostuff.compiler.model.Proto;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SchemaToProtoGenerator {
  record IdentifierRunnable(Schema<?> identifier, Runnable target)
      implements Runnable {

    @Override
    public void run() {
      this.target.run();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      IdentifierRunnable that = (IdentifierRunnable) o;
      return SwaggerUtils.schemaEquals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
      return SwaggerUtils.schemaHashCode(identifier);
    }
  }

  private final String protoPackage;

  private final OpenAPI openAPI;

  private final Schema<?> rootSchema;

  private final String rootName;

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
    createMessage(this.rootSchema);

    int iteration = 0;
    do {
      List<Runnable> oldPending = pending;
      pending = new ArrayList<>();
      for (Runnable runnable : oldPending) {
        runnable.run();
      }
      if (pending.size() >= oldPending.size()) {
        iteration++;
      }
    } while (!pending.isEmpty() && iteration < 1000);

    if (iteration == 1000) {
      throw new IllegalArgumentException(
          String.format("Failed to create schema %s. May be cyclic object.", this.rootName));
    }

    Map<String, Schema> wrap = new HashMap<>(1);
    wrap.put("value", this.rootSchema);
    createMessage(rootName, wrap, ProtoConst.ANNOTATION_WRAP_PROPERTY);

    return createProto();
  }

  protected Proto createProto() {
    StringBuilder sb = new StringBuilder();
    appendLine(sb, "syntax = \"proto3\";");
    if (StringUtils.isNotEmpty(protoPackage)) {
      sb.append("package ").append(protoPackage).append(";\n");
    }
    sb.append(msgStringBuilder);
    ProtoParser protoParser = new ProtoParser();
    return protoParser.parseFromContent(sb.toString());
  }

  private String findSchemaType(Schema<?> schema) {
    @SuppressWarnings("unchecked")
    String type = tryFindEnumType((List<String>) schema.getEnum());
    if (type != null) {
      return type;
    }

    type = findBaseType(schema.getType(), schema.getFormat());
    if (type != null) {
      return type;
    }

    Schema<?> itemProperty = schema.getItems();
    if (itemProperty != null) {
      String containerType = findArrayOrMapItemType(itemProperty);
      if (containerType != null) {
        return "repeated " + containerType;
      }
      return null;
    }

    itemProperty = (Schema<?>) schema.getAdditionalProperties();
    if (itemProperty != null) {
      String containerType = findArrayOrMapItemType(itemProperty);
      if (containerType != null) {
        return String.format("map<string, %s>", containerType);
      }
      return null;
    }

    type = schema.get$ref();
    if (type != null) {
      String typeName = type.substring(Components.COMPONENTS_SCHEMAS_REF.length());
      Schema<?> refSchema = openAPI.getComponents().getSchemas().get(typeName);
      if (refSchema == null) {
        throw new IllegalArgumentException("not found ref in components " + type);
      }
      if (StringUtils.isEmpty(refSchema.getName())) {
        refSchema.setName(typeName);
      }
      return findSchemaType(refSchema);
    }

    return findObjectType(schema);
  }

  private String findObjectType(Schema<?> schema) {
    String name = schema.getName();
    if (messages.contains(name)) {
      return name;
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
      case "integer:int32" -> "sint32";
      case "integer:int64" -> "sint64";
      case "integer:null" -> "string";  // BigInteger like values
      case "number:double" -> "double";
      case "number:float" -> "float";
      case "number:null" -> "string";  // BigDecimal like values
      case "string:null" -> "string";
      case "string:byte" -> "bytes";
      case "string:date" -> "int64";
      case "string:date-time" -> "int64";
      case "string:binary" -> throw new IllegalArgumentException("proto buffer not support file upload/download");
      default -> null;
    };
  }

  private String findArrayOrMapItemType(Schema<?> itemProperty) {
    // List<List<>>, need to wrap
    if (itemProperty.getItems() != null) {
      return findWrapPropertyType(List.class.getSimpleName(), itemProperty.getItems());
    }

    // List<Map<>>, need to wrap
    if (itemProperty.getAdditionalProperties() != null) {
      return findWrapPropertyType(Map.class.getSimpleName(), (Schema<?>) itemProperty.getAdditionalProperties());
    }

    return findSchemaType(itemProperty);
  }


  private String findWrapPropertyType(String prefix, Schema<?> property) {
    // List<List<>>, need to wrap
    if (property.getItems() != null) {
      return findWrapPropertyType(prefix + List.class.getSimpleName(), property.getItems());
    }

    // List<Map<>>, need to wrap
    if (property.getAdditionalProperties() != null) {
      return findWrapPropertyType(prefix + Map.class.getSimpleName(),
          (Schema<?>) property.getAdditionalProperties());
    }

    String type = findSchemaType(property);
    if (type == null) {
      return null;
    }

    // message name cannot have . (package separator)
    return prefix + StringUtils.capitalize(escapeMessageName(type));
  }

  public static String escapeMessageName(String name) {
    return name.replaceAll("\\.", "_");
  }


  private void wrapPropertyToMessage(String protoName, Schema<?> property) {
    createMessage(protoName, Collections.singletonMap("value", property), ProtoConst.ANNOTATION_WRAP_PROPERTY);
  }

  private void createMessage(String protoName, Map<String, Schema> properties, String... annotations) {
    for (String annotation : annotations) {
      msgStringBuilder.append("//");
      appendLine(msgStringBuilder, annotation);
    }
    appendLine(msgStringBuilder, "message %s {", protoName);
    int tag = 1;
    for (Entry<String, Schema> entry : properties.entrySet()) {
      Schema property = entry.getValue();
      String propertyType = findSchemaType(property);

      appendLine(msgStringBuilder, "  %s %s = %d;", propertyType, entry.getKey(), tag);
      tag++;
    }
    appendLine(msgStringBuilder, "}");
  }

  public void createMessage(Schema<?> schema) {
    String ref = schema.get$ref();
    if (ref != null) {
      String typeName = ref.substring(Components.COMPONENTS_SCHEMAS_REF.length());
      Schema<?> refSchema = openAPI.getComponents().getSchemas().get(typeName);
      if (refSchema == null) {
        throw new IllegalArgumentException("not found ref in components " + ref);
      }
      if (StringUtils.isEmpty(refSchema.getName())) {
        refSchema.setName(typeName);
      }
      createMessage(refSchema);
      return;
    }

    boolean wait = false;

    //array or map
    if (isArrayOrMap(schema)) {
      Schema<?> mapOrArrayItem = arrayOrMapItem(schema);
      if (findSchemaType(mapOrArrayItem) == null) {
        createMessageTask(mapOrArrayItem);
        wait = true;
      } else {
        if (createMapOrArrayMessageTask(mapOrArrayItem, true, schema)) {
          wait = true;
        }
      }
    }

    //object
    if (schema.getProperties() != null) {
      for (Entry<String, Schema> entry : schema.getProperties().entrySet()) {
        if (findSchemaType(entry.getValue()) == null) {
          createMessageTask(entry.getValue());
          wait = true;
        } else if (isArrayOrMap(entry.getValue())) {
          if (createMapOrArrayMessageTask(arrayOrMapItem(entry.getValue()), false, null)) {
            wait = true;
          }
        }
      }
    }

    if (wait) {
      IdentifierRunnable runnable = new IdentifierRunnable(schema, () -> createMessage(schema));
      if (!pending.contains(runnable)) {
        pending.add(runnable);
      }
      return;
    }

    if (findSchemaType(schema) != null) {
      return;
    }

    messages.add(schema.getName());

    appendLine(msgStringBuilder, "message %s {", schema.getName());
    int tag = 1;
    for (Entry<String, Schema> entry : schema.getProperties().entrySet()) {
      Schema property = entry.getValue();
      String propertyType = findSchemaType(property);

      appendLine(msgStringBuilder, "  %s %s = %d;", propertyType, entry.getKey(), tag);
      tag++;
    }
    appendLine(msgStringBuilder, "}");
  }

  private boolean isArrayOrMap(Schema<?> value) {
    return value.getItems() != null || value.getAdditionalProperties() != null;
  }

  private Schema<?> arrayOrMapItem(Schema<?> schema) {
    return schema.getItems() == null ?
        (Schema<?>) schema.getAdditionalProperties() : schema.getItems();
  }

  private void createMessageTask(Schema<?> schema) {
    IdentifierRunnable runnable = new IdentifierRunnable(schema, () -> createMessage(schema));
    if (!pending.contains(runnable)) {
      pending.add(runnable);
    }
  }

  private boolean createMapOrArrayMessageTask(Schema<?> schema, boolean nested, Schema<?> owner) {
    if (schema.getAdditionalProperties() != null) {
      String protoName = findWrapPropertyType(Map.class.getSimpleName(),
          (Schema<?>) schema.getAdditionalProperties());
      if (messages.add(protoName)) {
        pending.add(() -> wrapPropertyToMessage(protoName, schema));
        createMessageTask((Schema<?>) schema.getAdditionalProperties());
        return true;
      }
    }
    if (schema.getItems() != null) {
      String protoName = findWrapPropertyType(List.class.getSimpleName(), schema.getItems());
      if (messages.add(protoName)) {
        pending.add(() -> wrapPropertyToMessage(protoName, schema));
        createMessageTask(schema.getItems());
        return true;
      }
    }
    if (nested) {
      String protoName = owner.getAdditionalProperties() != null ?
          findWrapPropertyType(Map.class.getSimpleName(), schema) :
          findWrapPropertyType(List.class.getSimpleName(), schema);
      if (messages.add(protoName)) {
        pending.add(() -> wrapPropertyToMessage(protoName, owner));
        return true;
      }
    }
    return false;
  }
}
