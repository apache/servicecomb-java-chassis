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
package org.apache.servicecomb.core.provider;

import java.util.Set;

import org.apache.servicecomb.core.provider.OpenAPIRegistryManager.OpenAPIChangeListener;
import org.springframework.core.Ordered;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * Register and load OpenAPI extensions.
 */
public interface OpenAPIRegistry extends Ordered {
  boolean enabled();

  Set<String> getSchemaIds(String application, String serviceName);

  void registerOpenAPI(String application, String serviceName, String schemaId, OpenAPI api);

  OpenAPI loadOpenAPI(String appId, String microserviceName, String schemaId);

  void setOpenAPIChangeListener(OpenAPIChangeListener listener);
}
