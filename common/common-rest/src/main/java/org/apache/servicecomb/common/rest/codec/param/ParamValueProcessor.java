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

import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;

import com.fasterxml.jackson.databind.JavaType;

public interface ParamValueProcessor {
  Object getValue(HttpServletRequest request) throws Exception;

  void setValue(RestClientRequest clientRequest, Object arg) throws Exception;

  default Object convertValue(Object value, JavaType targetType) {
    if (value == null || targetType == null) {
      return value;
    }
    if (getSerialViewClass() != null) {
      return RestObjectMapperFactory.getRestViewMapper().setConfig(
          RestObjectMapperFactory.getRestViewMapper().getDeserializationConfig().withView(getSerialViewClass()))
          .convertValue(value, targetType);
    }
    return RestObjectMapperFactory.getRestObjectMapper()
        .convertValue(value, targetType);
  }

  String getParameterPath();

  String getProcessorType();

  default Class<?> getSerialViewClass() {
    return null;
  }
}
