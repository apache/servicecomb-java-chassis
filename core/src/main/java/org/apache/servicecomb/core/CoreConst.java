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

package org.apache.servicecomb.core;

import org.apache.servicecomb.registry.definition.DefinitionConst;

public final class CoreConst {
  private CoreConst() {
  }

  public static final String CSE_CONTEXT = "x-cse-context";

  public static final String RESTFUL = "rest";

  public static final String HIGHWAY = "highway";

  public static final String ANY_TRANSPORT = "";

  public static final String VERSION_RULE_LATEST = DefinitionConst.VERSION_RULE_LATEST;

  public static final String DEFAULT_VERSION_RULE = DefinitionConst.VERSION_RULE_ALL;

  public static final String PRODUCER_OPERATION = "producer-operation";

  public static final String CONSUMER_OPERATION = "consumer-operation";

  public static final String SRC_MICROSERVICE = "x-cse-src-microservice";

  public static final String SRC_SERVICE_ID = "x-src-serviceId";

  public static final String SRC_INSTANCE_ID = "x-src-instanceId";

  public static final String TARGET_MICROSERVICE = "x-cse-target-microservice";

  public static final String REMOTE_ADDRESS = "x-cse-remote-address";

  public static final String AUTH_TOKEN = "x-cse-auth-rsatoken";

  public static final String TRACE_ID_NAME = "X-B3-TraceId";

  // controlling whether to print stack information with sensitive errors
  public static final String PRINT_SENSITIVE_ERROR_MESSAGE = "servicecomb.error.printSensitiveErrorMessage";

  public static final String SWAGGER_EXPORT_ENABLED = "servicecomb.swagger.export.enabled";

  public static final String SWAGGER_DIRECTORY = "servicecomb.swagger.export.directory";
}
