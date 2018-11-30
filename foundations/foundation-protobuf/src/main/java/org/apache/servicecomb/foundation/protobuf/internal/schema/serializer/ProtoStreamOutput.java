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
package org.apache.servicecomb.foundation.protobuf.internal.schema.serializer;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.protostuff.ByteString;
import io.protostuff.LinkedBuffer;
import io.protostuff.Output;
import io.protostuff.ProtobufOutput;
import io.protostuff.Schema;

public class ProtoStreamOutput implements Output {
  private final LinkedBuffer linkedBuffer = LinkedBuffer.allocate();

  private final ProtobufOutput output = new ProtobufOutput(linkedBuffer);

  public byte[] toBytes() {
    return output.toByteArray();
  }

  public final void writeInt64(int fieldNumber, long value, boolean repeated) throws IOException {
    if (value != 0) {
      output.writeInt64(fieldNumber, value, repeated);
    }
  }

  public final void writeUInt64(int fieldNumber, long value, boolean repeated) throws IOException {
    if (value != 0) {
      output.writeUInt64(fieldNumber, value, repeated);
    }
  }

  public final void writeSInt64(int fieldNumber, long value, boolean repeated) throws IOException {
    if (value != 0) {
      output.writeSInt64(fieldNumber, value, repeated);
    }
  }

  public final void writeFixed64(int fieldNumber, long value, boolean repeated) throws IOException {
    if (value != 0) {
      output.writeFixed64(fieldNumber, value, repeated);
    }
  }

  public final void writeSFixed64(int fieldNumber, long value, boolean repeated) throws IOException {
    if (value != 0) {
      output.writeSFixed64(fieldNumber, value, repeated);
    }
  }

  public final void writeInt32(int fieldNumber, int value, boolean repeated) throws IOException {
    if (value != 0) {
      output.writeInt32(fieldNumber, value, repeated);
    }
  }

  public final void writeUInt32(int fieldNumber, int value, boolean repeated) throws IOException {
    if (value != 0) {
      output.writeUInt32(fieldNumber, value, repeated);
    }
  }

  public final void writeSInt32(int fieldNumber, int value, boolean repeated) throws IOException {
    if (value != 0) {
      output.writeSInt32(fieldNumber, value, repeated);
    }
  }

  public final void writeFixed32(int fieldNumber, int value, boolean repeated) throws IOException {
    if (value != 0) {
      output.writeFixed32(fieldNumber, value, repeated);
    }
  }

  public final void writeSFixed32(int fieldNumber, int value, boolean repeated) throws IOException {
    if (value != 0) {
      output.writeSFixed32(fieldNumber, value, repeated);
    }
  }

  public final void writeFloat(int fieldNumber, float value, boolean repeated) throws IOException {
    if (value != 0) {
      output.writeFloat(fieldNumber, value, repeated);
    }
  }

  public final void writeDouble(int fieldNumber, double value, boolean repeated) throws IOException {
    if (value != 0) {
      output.writeDouble(fieldNumber, value, repeated);
    }
  }

  public final void writeBool(int fieldNumber, boolean value, boolean repeated) throws IOException {
    if (value) {
      output.writeBool(fieldNumber, value, repeated);
    }
  }

  @Override
  public void writeEnum(int fieldNumber, int value, boolean repeated) throws IOException {

  }

  // write from String[0], maybe null
  public final void writeString(int fieldNumber, @Nullable String value, boolean repeated) throws IOException {
    if (value != null && !value.isEmpty()) {
      output.writeString(fieldNumber, value, repeated);
    }
  }

  @Override
  public void writeBytes(int fieldNumber, ByteString value, boolean repeated) throws IOException {
    output.writeBytes(fieldNumber, value, repeated);
  }

  public final void writeByteArray(int fieldNumber, @Nonnull byte[] value, boolean repeated) throws IOException {
    output.writeByteArray(fieldNumber, value, repeated);
  }

  @Override
  public void writeByteRange(boolean utf8String, int fieldNumber, byte[] value, int offset, int length,
      boolean repeated) throws IOException {
    output.writeByteRange(utf8String, fieldNumber, value, offset, length, repeated);
  }

  @Override
  public <T> void writeObject(int fieldNumber, T value, Schema<T> schema, boolean repeated) throws IOException {
    output.writeObject(fieldNumber, value, schema, repeated);
  }

  @Override
  public void writeBytes(int fieldNumber, ByteBuffer value, boolean repeated) throws IOException {
    output.writeBytes(fieldNumber, value, repeated);
  }
}
