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

package org.apache.servicecomb.swagger.extend;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implement this interface to create custom {@link ObjectMapper} for Swagger schema generation.
 */
public interface ModelResolveObjectMapperProvider {
  /**
   * @return order The smaller value it is, the higher priority this provider has.
   */
  default int getOrder() {
    return 0;
  }

  /**
   * @return the ObjectMapper, which is used by Swagger to introspect the param types and generate property collections.
   */
  ObjectMapper getMapper();
}
