/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.core;

import io.servicecomb.serviceregistry.definition.DefinitionConst;

public final class Const {
  private Const() {
  }

  public static final String CSE_CONTEXT = "x-cse-context";

  public static final String RESTFUL = "rest";

  public static final String ANY_TRANSPORT = "";

  public static final String VERSION_RULE_LATEST = DefinitionConst.VERSION_RULE_LATEST;

  public static final String DEFAULT_VERSION_RULE = VERSION_RULE_LATEST;

  public static final String PRODUCER_OPERATION = "producer-operation";

  public static final String SRC_MICROSERVICE = "x-cse-src-microservice";

  public static final String TARGET_MICROSERVICE = "x-cse-target-microservice";

  public static final String REMOTE_ADDRESS = "x-cse-remote-address";
}
