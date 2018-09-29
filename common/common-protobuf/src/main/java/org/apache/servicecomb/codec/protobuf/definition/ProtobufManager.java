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

package org.apache.servicecomb.codec.protobuf.definition;

import org.apache.servicecomb.codec.protobuf.utils.ScopedProtobufSchemaManager;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;

public final class ProtobufManager {
  private static ProtobufManager instance = new ProtobufManager();

  public static final String EXT_ID = "protobuf";

  private static final Object LOCK = new Object();

  private static ScopedProtobufSchemaManager defaultScopedProtobufSchemaManager = new ScopedProtobufSchemaManager(
      JvmUtils.findClassLoader());

  private ProtobufManager() {
  }

  /**
   * only for app classloader
   * @return
   */
  public static ScopedProtobufSchemaManager getDefaultScopedProtobufSchemaManager() {
    return defaultScopedProtobufSchemaManager;
  }

  public static OperationProtobuf getOrCreateOperation(OperationMeta operationMeta) throws Exception {
    OperationProtobuf operationProtobuf = operationMeta.getExtData(EXT_ID);
    if (operationProtobuf == null) {
      synchronized (LOCK) {
        MicroserviceMeta microserviceMeta = operationMeta.getMicroserviceMeta();
        ScopedProtobufSchemaManager scopedProtobufSchemaManager = microserviceMeta.getExtData(EXT_ID);
        if (scopedProtobufSchemaManager == null) {
          scopedProtobufSchemaManager = new ScopedProtobufSchemaManager(microserviceMeta.getClassLoader());
          microserviceMeta.putExtData(EXT_ID, scopedProtobufSchemaManager);
        }

        operationProtobuf = operationMeta.getExtData(EXT_ID);
        if (operationProtobuf == null) {
          operationProtobuf = new OperationProtobuf(scopedProtobufSchemaManager, operationMeta);
          operationMeta.putExtData(EXT_ID, operationProtobuf);
        }
      }
    }

    return operationProtobuf;
  }

  public static ProtobufManager getInstance() {
    return instance;
  }
}
