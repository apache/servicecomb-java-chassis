//========================================================================
//Copyright 2007-2009 David Yu dyuproject@gmail.com
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package io.protostuff.runtime;

import java.io.IOException;

import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.InputEx;
import io.protostuff.OutputEx;
import io.protostuff.ProtobufOutputEx;
import io.protostuff.WireFormat;
import io.protostuff.compiler.model.Field;

/**
 * Forked and modified from protostuff
 *
 * Represents a field of a message/pojo.
 */
public abstract class FieldSchema<T> {
  protected final Field protoField;

  protected final String name;

  protected final int fieldNumber;

  protected final int tag;

  protected final int tagSize;

  protected final boolean packed;

  protected final JavaType javaType;

  protected final boolean primitive;

  public FieldSchema(Field protoField, JavaType javaType) {
    this.protoField = protoField;
    this.name = protoField.getName();
    this.fieldNumber = protoField.getTag();
    this.packed = ProtoUtils.isPacked(protoField);

    int wireType = packed && protoField.isRepeated() ? WireFormat.WIRETYPE_LENGTH_DELIMITED
        : FieldTypeUtils.convert(protoField.getType()).wireType;
    this.tag = WireFormat.makeTag(fieldNumber, wireType);
    this.tagSize = ProtobufOutputEx.computeRawVarint32Size(tag);

    this.javaType = javaType;
    this.primitive = javaType.isPrimitive();
  }

  public int getFieldNumber() {
    return fieldNumber;
  }

  public Field getProtoField() {
    return protoField;
  }

  public int mergeFrom(InputEx input, T message) throws IOException {
    throw new UnsupportedOperationException();
  }

  public boolean isPrimitive() {
    return primitive;
  }

  public void getAndWriteTo(OutputEx output, T message) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * write from map
   *
   * @param output
   * @param value field value, will not be null
   * @throws IOException
   */
  public void writeTo(OutputEx output, Object value) throws IOException {
    throw new UnsupportedOperationException();
  }
}
