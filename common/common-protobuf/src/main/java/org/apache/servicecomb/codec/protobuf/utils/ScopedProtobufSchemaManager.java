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

package org.apache.servicecomb.codec.protobuf.utils;

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

public class ScopedProtobufSchemaManager {
  public  static final ScopedProtobufSchemaManager INSTANCE = new ScopedProtobufSchemaManager();
  private ScopedProtobufSchemaManager() {

  }

  // 适用于将单个类型包装的场景
  // 比如return
  public WrapSchema getOrCreateSchema(Type type) {
    // TODO: add implementation using new API
    return null;
  }

  public WrapSchema getOrCreateArgsSchema(OperationMeta operationMeta) {
    // TODO: add implementation using new API
    return null;
  }
}
