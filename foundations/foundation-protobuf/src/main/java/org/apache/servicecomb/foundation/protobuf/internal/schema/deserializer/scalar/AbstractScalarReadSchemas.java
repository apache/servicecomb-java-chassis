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
package org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar;

import org.apache.servicecomb.foundation.common.utils.bean.IntSetter;
import org.apache.servicecomb.foundation.common.utils.bean.LongSetter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;

import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class AbstractScalarReadSchemas {
  static abstract class AbstractIntSchema<T> extends FieldSchema<T> {
    protected final Setter<T, Integer> setter;

    public AbstractIntSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
      this.setter = propertyDescriptor.getSetter();
    }
  }

  static abstract class AbstractIntPrimitiveSchema<T> extends FieldSchema<T> {
    protected final IntSetter<T> setter;

    public AbstractIntPrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
      this.setter = propertyDescriptor.getSetter();
    }
  }

  static abstract class AbstractLongSchema<T> extends FieldSchema<T> {
    protected final Setter<T, Long> setter;

    public AbstractLongSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
      this.setter = propertyDescriptor.getSetter();
    }
  }

  static abstract class AbstractLongPrimitiveSchema<T> extends FieldSchema<T> {
    protected final LongSetter<T> setter;

    public AbstractLongPrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
      this.setter = propertyDescriptor.getSetter();
    }
  }
}
