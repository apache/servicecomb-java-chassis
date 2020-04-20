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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.codec.protobuf.utils.ScopedProtobufSchemaManager;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.swagger.invocation.InvocationType;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.annotations.VisibleForTesting;

public final class ProtobufManager {
  public static final String EXT_ID = "protobuf";

  private static final Object LOCK = new Object();

  static class RuntimeCacheKey {
    final InvocationType invocationType;

    final String uniqueOperationId;

    // Using response type as the cache key.
    // May consider request type as well, but now not implemented
    final JavaType responseType;

    public RuntimeCacheKey(InvocationType invocationType, String operationId, JavaType responseType) {
      this.invocationType = invocationType;
      this.uniqueOperationId = operationId;
      this.responseType = responseType;
    }


    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      RuntimeCacheKey that = (RuntimeCacheKey) o;

      if (invocationType != that.invocationType) {
        return false;
      }
      if (!uniqueOperationId.equals(that.uniqueOperationId)) {
        return false;
      }
      return responseType != null ? responseType.equals(that.responseType)
          : that.responseType == null;
    }

    @Override
    public int hashCode() {
      int result = invocationType.hashCode();
      result = 31 * result + uniqueOperationId.hashCode();
      result = 31 * result + (responseType != null ? responseType.hashCode() : 0);
      return result;
    }
  }

  private static final Map<RuntimeCacheKey, OperationProtobuf> RUNTIME_CACHE = new HashMap<>();

  public static OperationProtobuf getOrCreateOperation(Invocation invocation) {
    RuntimeCacheKey cacheKey = new RuntimeCacheKey(invocation.getInvocationType(),
        invocation.getOperationMeta().getMicroserviceQualifiedName(),
        invocation.findResponseType(Status.OK.getStatusCode()));
    OperationProtobuf operationProtobuf = RUNTIME_CACHE.get(cacheKey);
    if (operationProtobuf == null) {
      synchronized (LOCK) {
        MicroserviceMeta microserviceMeta = invocation.getMicroserviceMeta();
        ScopedProtobufSchemaManager scopedProtobufSchemaManager = microserviceMeta.getExtData(EXT_ID);
        if (scopedProtobufSchemaManager == null) {
          scopedProtobufSchemaManager = new ScopedProtobufSchemaManager();
          microserviceMeta.putExtData(EXT_ID, scopedProtobufSchemaManager);
        }

        operationProtobuf = RUNTIME_CACHE.get(cacheKey);
        if (operationProtobuf == null) {
          operationProtobuf = new OperationProtobuf(scopedProtobufSchemaManager, invocation);
          RUNTIME_CACHE.put(cacheKey, operationProtobuf);
        }
      }
    }

    return operationProtobuf;
  }

  @VisibleForTesting
  public static void clear() {
    RUNTIME_CACHE.clear();
  }
}
