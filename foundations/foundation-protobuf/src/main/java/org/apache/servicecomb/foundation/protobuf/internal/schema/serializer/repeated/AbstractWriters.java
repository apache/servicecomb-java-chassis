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
import io.protostuff.ProtobufOutputEx;
import io.protostuff.SchemaWriter;
import io.protostuff.WireFormat;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldTypeUtils;

public abstract class AbstractWriters<T> {
  protected final Field protoField;

  protected final int tag;

  protected final int tagSize;

  public SchemaWriter<T[]> arrayWriter;

  public SchemaWriter<Collection<T>> collectionWriter;

  public SchemaWriter<String[]> stringArrayWriter;

  public final Class<T[]> arrayClass;

  @SuppressWarnings("unchecked")
  public AbstractWriters(Field protoField) {
    this(protoField, null);
  }

  public AbstractWriters(Field protoField, Class<T[]> arrayClass) {
    this.protoField = protoField;
    int wireType = ProtoUtils.isPacked(protoField) && protoField.isRepeated() ? WireFormat.WIRETYPE_LENGTH_DELIMITED
        : FieldTypeUtils.convert(protoField.getType()).wireType;
    this.tag = WireFormat.makeTag(protoField.getTag(), wireType);
    this.tagSize = ProtobufOutputEx.computeRawVarint32Size(tag);

    if (arrayClass == null) {
      arrayClass = getFieldArgument(this.getClass(), "arrayWriter");
    }
    this.arrayClass = arrayClass;
  }

  @SuppressWarnings("unchecked")
  public void dynamicWriteTo(OutputEx output, Object value) throws IOException {
    if (value instanceof Collection) {
      collectionWriter.writeTo(output, (Collection<T>) value);
      return;
    }

    // from normal model
    if (arrayClass.isInstance(value)) {
      arrayWriter.writeTo(output, (T[]) value);
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
