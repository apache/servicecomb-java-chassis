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

package org.apache.servicecomb.swagger.converter;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.SwaggerUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

@SuppressWarnings("rawtypes")
public abstract class AbstractConverter implements Converter {
  protected abstract Map<String, Object> findVendorExtensions(Object def);

  protected abstract JavaType doConvert(OpenAPI swagger, Schema def);

  @Override
  public JavaType convert(OpenAPI swagger, Schema def) {
    Map<String, Object> vendorExtensions = findVendorExtensions(def);
    String canonical = SwaggerUtils.getClassName(vendorExtensions);
    if (StringUtils.isEmpty(canonical)) {
      return doConvert(swagger, def);
    }

    try {
      return TypeFactory.defaultInstance().constructFromCanonical(canonical);
    } catch (Throwable e) {
      return doConvert(swagger, def);
    }
  }
}
