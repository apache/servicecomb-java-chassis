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

package org.apache.servicecomb.common.rest;

public final class RestConst {
  private RestConst() {
  }

  public static final String REST_CLIENT_REQUEST_PATH = "rest-client-request-path";

  public static final String SWAGGER_REST_OPERATION = "swaggerRestOperation";

  public static final String REST = "rest";

  public static final String SCHEME = "cse";

  public static final String URI_PREFIX = SCHEME + "://";

  // in HttpServletRequest attribute
  public static final String PATH_PARAMETERS = "servicecomb-paths";

  // in HttpServletRequest attribute
  public static final String BODY_PARAMETER = "servicecomb-body";

  // in HttpServletRequest attribute
  public static final String FORM_PARAMETERS = "servicecomb-forms";

  public static final String REST_PRODUCER_INVOCATION = "servicecomb-rest-producer-invocation";

  public static final String REST_REQUEST = "servicecomb-rest-request";

  public static final String CONSUMER_HEADER = "servicecomb-rest-consumer-header";
}
