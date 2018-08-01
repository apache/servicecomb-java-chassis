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
package org.apache.servicecomb.swagger.invocation.arguments;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;

public class ProviderParameter {
  private int index;

  private Type type;

  /**
   * the param name specified by param annotations(i.e. the param name in schema), or the param name defined in code
   */
  private String name;

  private Annotation[] annotations;

  public ProviderParameter(int index, Type type, String name) {
    this.index = index;
    this.type = type;
    this.name = name;
  }

  public int getIndex() {
    return index;
  }

  public ProviderParameter setIndex(int index) {
    this.index = index;
    return this;
  }

  public Type getType() {
    return type;
  }

  public ProviderParameter setType(Type type) {
    this.type = type;
    return this;
  }

  public String getName() {
    return name;
  }

  public ProviderParameter setName(String name) {
    this.name = name;
    return this;
  }

  public Annotation[] getAnnotations() {
    return annotations;
  }

  public ProviderParameter setAnnotations(Annotation[] annotations) {
    this.annotations = annotations;
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ProviderParameter{");
    sb.append("index=").append(index);
    sb.append(", type=").append(type);
    sb.append(", name='").append(name).append('\'');
    sb.append(", annotations=").append(Arrays.toString(annotations));
    sb.append('}');
    return sb.toString();
  }
}
