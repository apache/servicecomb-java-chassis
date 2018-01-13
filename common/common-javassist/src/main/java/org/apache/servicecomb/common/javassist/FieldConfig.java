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

package org.apache.servicecomb.common.javassist;

import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.databind.JavaType;

public class FieldConfig {
  private String name;

  // javassist的成员不支持int这样的类型，必须是Integer才行
  private Class<?> rawType;

  private JavaType type;

  private boolean genGetter;

  private boolean genSetter;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Class<?> getRawType() {
    return rawType;
  }

  public JavaType getType() {
    return type;
  }

  public void setType(JavaType type) {
    this.rawType = ClassUtils.resolvePrimitiveIfNecessary(type.getRawClass());
    this.type = type;
  }

  public boolean isGenGetter() {
    return genGetter;
  }

  public void setGenGetter(boolean genGetter) {
    this.genGetter = genGetter;
  }

  public boolean isGenSetter() {
    return genSetter;
  }

  public void setGenSetter(boolean genSetter) {
    this.genSetter = genSetter;
  }

  public String getGenericSignature() {
    if (type.hasGenericTypes()) {
      return type.getGenericSignature();
    }

    return null;
  }
}
