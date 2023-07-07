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
package org.apache.servicecomb.swagger.generator.core.model;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public enum HttpParameterType {
  /** io.swagger.v3.oas.annotations.enums.ParameterIn.PATH */
  PATH,
  /** io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY */
  QUERY,
  /** io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER */
  HEADER,
  /** io.swagger.v3.oas.annotations.enums.ParameterIn.COOKIE */
  COOKIE,

  /** request bodies: application/x-www-form-urlencoded, multipart/form-data */
  FORM,

  /** request bodies: application/json, etc. */
  BODY;

  public static HttpParameterType parse(String value) {
    if (StringUtils.isEmpty(value)) {
      return null;
    }

    return HttpParameterType.valueOf(value.toUpperCase(Locale.US));
  }

  public static boolean isBodyParameter(HttpParameterType type) {
    return BODY == type || FORM == type;
  }
}
