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

package org.apache.servicecomb.serviceregistry.definition;

public interface DefinitionConst {

  String CONFIG_QUALIFIED_INSTANCE_ENVIRONMENT_KEY = "instance_description.environment";

  String CONFIG_ALLOW_CROSS_APP_KEY = "allowCrossApp";

  String DEFAULT_APPLICATION_ID = "default";

  String DEFAULT_MICROSERVICE_VERSION = "1.0.0";

  String DEFAULT_STAGE = "prod";

  String DEFAULT_INSTANCE_ENVIRONMENT = "production";

  String VERSION_RULE_LATEST = "latest";

  String VERSION_RULE_ALL = "0.0.0+";
}
