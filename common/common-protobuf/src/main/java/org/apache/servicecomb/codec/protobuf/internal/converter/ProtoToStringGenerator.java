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

import io.protostuff.compiler.model.Enum;
import io.protostuff.compiler.model.EnumConstant;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Import;
import io.protostuff.compiler.model.Message;
import io.protostuff.compiler.model.Proto;
import io.protostuff.compiler.model.Service;
import io.protostuff.compiler.model.ServiceMethod;

public class ProtoToStringGenerator {
  private final Proto proto;

  public ProtoToStringGenerator(Proto proto) {
    this.proto = proto;
  }

  public String protoToString() {
    StringBuilder sb = new StringBuilder();
    appendLine(sb, "syntax = \"%s\";", proto.getSyntax());
    for (Import importValue : proto.getImports()) {
      appendLine(sb, "import \"%s\";", importValue.getValue());
    }
    appendLine(sb, "package %s;\n", proto.getPackage().getValue());

    for (Message message : proto.getMessages()) {
      messageToString(message, sb);
    }

    for (Enum enumValue : proto.getEnums()) {
      enumToString(enumValue, sb);
    }

    for (Service service : proto.getServices()) {
      serviceToString(service, sb);
    }
    return sb.toString();
  }

  private void serviceToString(Service service, StringBuilder sb) {
    appendLine(sb, "service %s {", service.getName());
    for (ServiceMethod serviceMethod : service.getMethods()) {
      if (!serviceMethod.getCommentLines().isEmpty()) {
        appendLine(sb, "  //" + serviceMethod.getComments());
      }
      appendLine(sb, "  rpc %s (%s) returns (%s);\n", serviceMethod.getName(), serviceMethod.getArgTypeName(),
          serviceMethod.getReturnTypeName());
    }
    if (!service.getMethods().isEmpty()) {
      sb.setLength(sb.length() - 1);
    }
    appendLine(sb, "}");
  }

  protected void enumToString(Enum enumValue, StringBuilder sb) {
    appendLine(sb, "enum %s {", enumValue.getName());
    for (EnumConstant enumConstant : enumValue.getConstants()) {
      appendLine(sb, "  %s = %s;", enumConstant.getName(), enumConstant.getValue());
    }
    sb.append("}\n\n");
  }

  private void messageToString(Message message, StringBuilder sb) {
    appendLine(sb, "message %s {", message.getName());
    for (Field field : message.getFields()) {
      sb.append("  ");
      fieldToString(field, field.isRepeated(), sb);
    }
    appendLine(sb, "}\n");
  }

  private void fieldToString(Field field, boolean repeated, StringBuilder sb) {
    if (field.isMap()) {
      fieldMapToString(field, sb);
      return;
    }

    if (repeated) {
      fieldRepeatedToString(field, sb);
      return;
    }

    appendLine(sb, "%s %s = %d;", field.getTypeName(), field.getName(), field.getTag());
  }

  private void fieldRepeatedToString(Field field, StringBuilder sb) {
    sb.append("repeated ");
    fieldToString(field, false, sb);
  }

  private void fieldMapToString(Field field, StringBuilder sb) {
    Message entryMessage = (Message) field.getType();
    Field keyField = entryMessage.getField(1);
    Field valueField = entryMessage.getField(2);

    // map<string, string> name = 1;
    appendLine(sb, "map<%s, %s> %s = %d;", keyField.getTypeName(), valueField.getTypeName(), field.getName(),
        field.getTag());
  }
}
