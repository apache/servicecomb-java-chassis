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

package org.apache.servicecomb.common.rest.definition.path;

import java.util.Map;

import org.apache.servicecomb.common.rest.definition.RestParam;

public abstract class AbstractUrlParamWriter implements UrlParamWriter {
  protected RestParam param;

  protected Object getParamValue(Map<String, Object> args) {
    if (param == null) {
      // Wrong server definition
      //  @GetMapping(path = "/getLocalDateTime/{paramX}")
      //  public LocalDateTime getLocalDateTimePath(@PathParam("paramY") LocalDateTime date) {
      throw new IllegalArgumentException("Path parameter name not valid in provider. Check if provider "
          + "path pattern has the parameter name.");
    }
    return args.get(param.getParamName());
  }
}
