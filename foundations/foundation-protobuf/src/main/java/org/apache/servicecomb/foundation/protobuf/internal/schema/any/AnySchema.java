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
package org.apache.servicecomb.foundation.protobuf.internal.schema.any;

import java.io.IOException;

import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;

import io.protostuff.InputEx;
import io.protostuff.OutputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class AnySchema<T> extends FieldSchema<T> {
  private final AnyEntrySchema anyEntrySchema;

  private final Getter<T, Object> getter;

  private final Setter<T, Object> setter;

  public AnySchema(ProtoMapper protoMapper, Field protoField, PropertyDescriptor propertyDescriptor) {
    super(protoField, propertyDescriptor.getJavaType());

    this.anyEntrySchema = new AnyEntrySchema(protoMapper);
    this.getter = propertyDescriptor.getGetter();
    this.setter = propertyDescriptor.getSetter();
  }

  @Override
  public final int mergeFrom(InputEx input, T message) throws IOException {
    Object anyValue = anyEntrySchema.deseriaze(input);
    setter.set(message, anyValue);

    return input.readFieldNumber();
  }

  @Override
  public void getAndWriteTo(OutputEx output, T message) throws IOException {
    Object anyEntry = getter.get(message);
    if (anyEntry == null) {
      return;
    }

    output.writeObject(tag, tagSize, anyEntry, anyEntrySchema);
  }

  @Override
  public final void writeTo(OutputEx output, Object value) throws IOException {
    output.writeObject(tag, tagSize, value, anyEntrySchema);
  }
}
