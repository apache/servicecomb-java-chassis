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
package org.apache.servicecomb.foundation.protobuf.internal.schema.scalar;

import java.io.IOException;

import org.apache.servicecomb.foundation.protobuf.internal.schema.FieldSchema;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.compiler.model.Field;

public class Int32Schema extends FieldSchema {
  public Int32Schema(Field protoField) {
    super(protoField);
  }

  @Override
  public Object readFrom(Input input) throws IOException {
    return input.readInt32();
  }

  @Override
  public void mergeFrom(Input input, Object message) throws IOException {
    setter.set(message, input.readInt32());
  }

  @Override
  public void writeTo(Output output, Object value) throws IOException {
    if (value == null) {
      return;
    }

    if (value instanceof Number) {
      output.writeInt32(number, ((Number) value).intValue(), repeated);
      return;
    }

    if (value instanceof String[]) {
      if (((String[]) value).length == 0) {
        return;
      }
      int v = Integer.parseInt(((String[]) value)[0], 10);
      output.writeInt32(number, v, repeated);
      return;
    }

    if (value instanceof String) {
      int v = Integer.parseInt((String) value, 10);
      output.writeInt32(number, v, repeated);
      return;
    }

    throwNotSupportValue(value);
  }
}
