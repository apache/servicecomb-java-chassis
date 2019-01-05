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
package org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated;

import static org.apache.servicecomb.foundation.common.utils.ReflectUtils.getFieldArgument;

import java.util.Collection;

import io.protostuff.compiler.model.Field;

public abstract class AbstractReaders<T> {
  protected final Field protoField;

  protected final int fieldNumber;

  public RepeatedReader<Collection<T>> collectionReader;

  public final Class<T[]> arrayClass;

  @SuppressWarnings("unchecked")
  public AbstractReaders(Field protoField) {
    this(protoField, null);
  }

  public AbstractReaders(Field protoField, Class<T[]> arrayClass) {
    this.protoField = protoField;
    this.fieldNumber = protoField.getTag();

    if (arrayClass == null) {
      arrayClass = getFieldArgument(this.getClass(), "arrayClass");
    }
    this.arrayClass = arrayClass;
  }
}
