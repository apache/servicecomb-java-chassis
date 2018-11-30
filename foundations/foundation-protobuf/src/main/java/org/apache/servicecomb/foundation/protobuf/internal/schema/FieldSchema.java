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

import java.io.IOException;

import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.apache.servicecomb.foundation.protobuf.internal.bean.BeanFactory;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.ProtoStreamOutput;

import com.google.common.annotations.VisibleForTesting;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Type;

public abstract class FieldSchema extends io.protostuff.runtime.Field<Object> {
  protected final Field protoField;

  protected Getter getter;

  protected Setter setter;

  protected BeanFactory factory;

  public FieldSchema(Field protoField) {
    super(ProtoSchemaUtils.convert(protoField.getType()),
        protoField.getTag(),
        protoField.getName(),
        protoField.isRepeated(),
        null);
    this.protoField = protoField;
  }

  public Field getProtoField() {
    return protoField;
  }

  public Getter getGetter() {
    return getter;
  }

  public void setGetter(Getter getter) {
    this.getter = getter;
  }

  public Setter getSetter() {
    return setter;
  }

  public void setSetter(Setter setter) {
    this.setter = setter;
  }

  public void setFactory(BeanFactory factory) {
    this.factory = factory;
  }

  protected void throwNotSupportValue(Object value) throws IllegalStateException {
    throw new IllegalStateException(
        String.format("not support serialize from %s to proto %s, field=%s:%s",
            value.getClass().getName(),
            protoField.getTypeName(),
            ((Type) protoField.getParent()).getCanonicalName(),
            protoField.getName()));
  }

  @SuppressWarnings("unchecked")
  protected <T> T getOrCreateFieldValue(Object message) {
    Object value = getter.get(message);
    if (value == null) {
      value = this.factory.create();
      setter.set(message, value);
    }
    return (T) value;
  }

  @Override
  protected void transfer(Pipe pipe, Input input, Output output, boolean repeated) {
    throw new UnsupportedOperationException();
  }

  public abstract Object readFrom(Input input) throws IOException;

  @VisibleForTesting
  public byte[] writeTo(Object message) throws IOException {
    ProtoStreamOutput output = new ProtoStreamOutput();
    writeTo(output, message);
    return output.toBytes();
  }

  @Override
  public abstract void writeTo(Output output, Object message) throws IOException;

  @Override
  public abstract void mergeFrom(Input input, Object message) throws IOException;
}
