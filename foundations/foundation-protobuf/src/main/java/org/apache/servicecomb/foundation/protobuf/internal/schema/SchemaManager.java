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

import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoConst;
import org.apache.servicecomb.foundation.protobuf.internal.schema.scalar.BoolSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.scalar.BytesSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.scalar.DoubleSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.scalar.Fixed32Schema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.scalar.Fixed64Schema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.scalar.FloatSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.scalar.Int32Schema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.scalar.Int64Schema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.scalar.SFixed32Schema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.scalar.SFixed64Schema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.scalar.SInt32Schema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.scalar.SInt64Schema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.scalar.StringSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.scalar.UInt32Schema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.scalar.UInt64Schema;

import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Proto;
import io.protostuff.compiler.model.ScalarFieldType;

public class SchemaManager {
  protected final ProtoMapper protoMapper;

  protected final Proto proto;

  public SchemaManager(ProtoMapper protoMapper) {
    this.protoMapper = protoMapper;
    this.proto = protoMapper.getProto();
  }

  protected boolean isAnyField(Field protoField, boolean repeated) {
    return !repeated && protoField.getType().getCanonicalName().equals(ProtoConst.ANY.getCanonicalName());
  }

  protected FieldSchema createScalarField(Field protoField) {
    switch ((ScalarFieldType) protoField.getType()) {
      case INT32:
        return new Int32Schema(protoField);
      case UINT32:
        return new UInt32Schema(protoField);
      case SINT32:
        return new SInt32Schema(protoField);
      case FIXED32:
        return new Fixed32Schema(protoField);
      case SFIXED32:
        return new SFixed32Schema(protoField);
      case INT64:
        return new Int64Schema(protoField);
      case UINT64:
        return new UInt64Schema(protoField);
      case SINT64:
        return new SInt64Schema(protoField);
      case FIXED64:
        return new Fixed64Schema(protoField);
      case SFIXED64:
        return new SFixed64Schema(protoField);
      case FLOAT:
        return new FloatSchema(protoField);
      case DOUBLE:
        return new DoubleSchema(protoField);
      case BOOL:
        return new BoolSchema(protoField);
      case STRING:
        return new StringSchema(protoField);
      case BYTES:
        return new BytesSchema(protoField);
      default:
        throw new IllegalStateException("unknown proto field type: " + protoField.getType());
    }
  }
}
