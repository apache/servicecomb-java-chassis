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
package org.apache.servicecomb.foundation.protobuf.internal.schema;

import io.protostuff.WireFormat.FieldType;
import io.protostuff.compiler.model.ScalarFieldType;

public final class ProtoSchemaUtils {
  public static FieldType convert(io.protostuff.compiler.model.FieldType fieldType) {
    if (fieldType.isEnum()) {
      return FieldType.ENUM;
    }

    if (fieldType.isScalar()) {
      switch ((ScalarFieldType) fieldType) {
        case INT32:
          return FieldType.INT32;
        case INT64:
          return FieldType.INT64;
        case UINT32:
          return FieldType.UINT32;
        case UINT64:
          return FieldType.UINT64;
        case SINT32:
          return FieldType.SINT32;
        case SINT64:
          return FieldType.SINT64;
        case FIXED32:
          return FieldType.FIXED32;
        case FIXED64:
          return FieldType.FIXED64;
        case SFIXED32:
          return FieldType.SFIXED32;
        case SFIXED64:
          return FieldType.SFIXED64;
        case FLOAT:
          return FieldType.FLOAT;
        case DOUBLE:
          return FieldType.DOUBLE;
        case BOOL:
          return FieldType.BOOL;
        case STRING:
          return FieldType.STRING;
        case BYTES:
          return FieldType.BYTES;
        default:
          throw new IllegalStateException("bug: miss process of " + fieldType);
      }
    }

    if (fieldType.isMessage()) {
      return FieldType.MESSAGE;
    }
    
    throw new IllegalStateException("bug: miss process of " + fieldType);
  }
}
