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
package org.apache.servicecomb.foundation.common.utils.bean;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SetterWrapper {
  private Object setter;

  public SetterWrapper(Object setter) {
    this.setter = setter;
  }

  public void set(Object instance, Object value) {
    if (setter instanceof Setter) {
      ((Setter) setter).set(instance, value);
    } else if (setter instanceof BoolSetter) {
      ((BoolSetter) setter).set(instance, (boolean) value);
    } else if (setter instanceof ByteSetter) {
      ((ByteSetter) setter).set(instance, (byte) value);
    } else if (setter instanceof CharSetter) {
      ((CharSetter) setter).set(instance, (char) value);
    } else if (setter instanceof ShortSetter) {
      ((ShortSetter) setter).set(instance, (short) value);
    } else if (setter instanceof IntSetter) {
      ((IntSetter) setter).set(instance, (int) value);
    } else if (setter instanceof LongSetter) {
      ((LongSetter) setter).set(instance, (long) value);
    } else if (setter instanceof FloatSetter) {
      ((FloatSetter) setter).set(instance, (float) value);
    } else if (setter instanceof DoubleSetter) {
      ((DoubleSetter) setter).set(instance, (double) value);
    } else {
      throw new IllegalStateException("unexpected setter " + setter.getClass().getName());
    }
  }
}
