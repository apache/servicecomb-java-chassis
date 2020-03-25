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

package org.apache.servicecomb.provider.pojo.definition;

/**
 * In consumer, every schema may have many consumer interfaces, each interface may contain part of the
 * scheme operations.
 */
public class PojoConsumerMapKey {
  private final String operationId;

  private final Class<?> consumerIntf;

  public PojoConsumerMapKey(String operationId, Class<?> consumerIntf) {
    this.operationId = operationId;
    this.consumerIntf = consumerIntf;
  }

  public String getOperationId() {
    return operationId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PojoConsumerMapKey that = (PojoConsumerMapKey) o;

    if (!operationId.equals(that.operationId)) {
      return false;
    }
    return consumerIntf.equals(that.consumerIntf);
  }

  @Override
  public int hashCode() {
    int result = operationId.hashCode();
    result = 31 * result + consumerIntf.hashCode();
    return result;
  }

  public Class<?> getConsumerIntf() {
    return consumerIntf;
  }
}
