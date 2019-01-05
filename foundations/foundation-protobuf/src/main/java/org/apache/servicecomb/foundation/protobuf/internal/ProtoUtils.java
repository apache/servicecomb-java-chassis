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
package org.apache.servicecomb.foundation.protobuf.internal;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.compiler.model.DynamicMessage;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.FieldContainer;
import io.protostuff.compiler.model.ScalarFieldType;
import io.protostuff.compiler.model.Type;

public final class ProtoUtils {
  private ProtoUtils() {
  }

  public static boolean isAnyField(Field protoField) {
    return protoField.getType().getCanonicalName().equals(ProtoConst.ANY.getCanonicalName());
  }

  public static boolean isWrapProperty(FieldContainer fieldContainer) {
    return fieldContainer.getCommentLines().contains(ProtoConst.ANNOTATION_WRAP_PROPERTY);
  }

  /**
   * all supported type, default to packed
   * @param protoField
   * @return
   */
  public static boolean isSupportPacked(Field protoField) {
    if (protoField.getType().isEnum()) {
      return true;
    }

    if (protoField.getType().isScalar()) {
      ScalarFieldType scalarFieldType = (ScalarFieldType) protoField.getType();
      return scalarFieldType != ScalarFieldType.STRING && scalarFieldType != ScalarFieldType.BYTES;
    }

    return false;
  }

  public static boolean isPacked(Field protoField) {
    DynamicMessage.Value dynamicMessageValue = protoField.getOptions().get("packed");
    if (dynamicMessageValue != null) {
      return dynamicMessageValue.getBoolean();
    }

    return isSupportPacked(protoField);
  }

  public static void throwNotSupportWrite(Field protoField, Object value) throws IllegalStateException {
    throwNotSupportWrite(protoField, value.getClass());
  }

  public static void throwNotSupportWrite(Field protoField, Class<?> cls) throws IllegalStateException {
    throw new IllegalStateException(
        String.format("not support serialize from %s to proto %s, field=%s:%s",
            cls.getName(),
            protoField.getTypeName(),
            ((Type) protoField.getParent()).getCanonicalName(),
            protoField.getName()));
  }

  public static void throwNotSupportMerge(Field protoField, JavaType javaType) throws IllegalStateException {
    throw new IllegalStateException(
        String.format("not support deserialize proto %s as %s, field=%s:%s",
            protoField.getTypeName(),
            javaType.toCanonical(),
            ((Type) protoField.getParent()).getCanonicalName(),
            protoField.getName()));
  }

  public static void throwNotSupportNullElement(Field protoField) throws IllegalStateException {
    throw new IllegalStateException(
        String.format("not support serialize null element, field=%s:%s",
            ((Type) protoField.getParent()).getCanonicalName(),
            protoField.getName()));
  }
}
