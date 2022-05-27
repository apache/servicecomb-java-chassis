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

package org.apache.servicecomb.common.rest.codec.param;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Defaults;

public abstract class AbstractParamProcessor implements ParamValueProcessor {
  protected String paramPath;

  // for consumer, targetType should be null
  protected JavaType targetType;

  protected Object defaultValue;

  protected boolean required = false;

  public Object getDefaultValue() {
    return defaultValue;
  }

  public AbstractParamProcessor(String paramPath, JavaType targetType, Object defaultValue, boolean required) {
    this.paramPath = paramPath;
    this.targetType = targetType;
    this.defaultValue = defaultValue;
    this.required = required;
    if (defaultValue == null &&
        targetType != null && targetType.getRawClass().isPrimitive()) {
      this.defaultValue = Defaults.defaultValue(targetType.getRawClass());
    }
  }

  @Override
  public String getParameterPath() {
    return paramPath;
  }

  public JavaType getTargetType() {
    return targetType;
  }

  public boolean isRequired() {
    return required;
  }
}
