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

package org.apache.servicecomb.swagger.generator;

import javax.ws.rs.core.Response;

public final class SwaggerConst {
  private SwaggerConst() {

  }

  public static final String SUCCESS_KEY = String.valueOf(Response.Status.OK.getStatusCode());

  public static final String EXT_JAVA_INTF = "x-java-interface";

  public static final String EXT_JAVA_CLASS = "x-java-class";

  public static final String EXT_RAW_JSON_TYPE = "x-raw-json";

  public static final String EXT_JSON_VIEW = "x-json-view";
}
