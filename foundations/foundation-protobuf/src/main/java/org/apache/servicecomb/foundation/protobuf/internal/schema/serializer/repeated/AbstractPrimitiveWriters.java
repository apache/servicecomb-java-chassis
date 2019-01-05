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
package org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated;

import static org.apache.servicecomb.foundation.common.utils.ReflectUtils.getFieldArgument;

import java.io.IOException;
import java.util.Collection;

import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;

import io.protostuff.OutputEx;
import io.protostuff.SchemaWriter;
import io.protostuff.compiler.model.Field;

public abstract class AbstractPrimitiveWriters<PRIMITIVE_ARRAY, PRIMITIVE_WRAPPER> extends
    AbstractWriters<PRIMITIVE_WRAPPER> {
  public SchemaWriter<PRIMITIVE_ARRAY> primitiveArrayWriter;

  public final Class<PRIMITIVE_ARRAY> primitiveArrayClass;

  @SuppressWarnings("unchecked")
  public AbstractPrimitiveWriters(Field protoField) {
    super(protoField);

    this.primitiveArrayClass = getFieldArgument(this.getClass(), "primitiveArrayClass");
  }

  @SuppressWarnings("unchecked")
  public final void dynamicWriteTo(OutputEx output, Object value) throws IOException {
    // from normal model
    if (primitiveArrayClass.isInstance(value)) {
      primitiveArrayWriter.writeTo(output, (PRIMITIVE_ARRAY) value);
      return;
    }

    if (arrayClass.isInstance(value)) {
      arrayWriter.writeTo(output, (PRIMITIVE_WRAPPER[]) value);
      return;
    }

    if (value instanceof Collection) {
      collectionWriter.writeTo(output, (Collection<PRIMITIVE_WRAPPER>) value);
      return;
    }

    // from http request
    if (value instanceof String[]) {
      stringArrayWriter.writeTo(output, (String[]) value);
      return;
    }

    ProtoUtils.throwNotSupportWrite(protoField, value);
  }
}
