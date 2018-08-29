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

package org.apache.servicecomb.core.tracing;

public interface TraceIdGenerator {
  default int getOrder() {
    return 1000;
  }

  /**
   * <pre>
   *   for generators have the same name, will only use the minimum order instance
   *   not use getTraceIdKeyName to control this logic, because most customers not want to generate multiple traceIds
   * </pre>
   * @return generator name
   */
  default String getName() {
    return "default";
  }

  /**
   *
   * @return trance id key name
   * <pre>
   * default value is X-B3-TraceId to work with zipkin
   * </pre>
   */
  String getTraceIdKeyName();

  String generate();
}
