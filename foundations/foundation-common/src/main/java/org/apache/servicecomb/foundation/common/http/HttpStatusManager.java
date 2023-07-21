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
package org.apache.servicecomb.foundation.common.http;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;

public class HttpStatusManager {
  private final Map<Integer, StatusType> statusMap = new ConcurrentHashMap<>();

  public HttpStatusManager() {
    for (Status status : Status.values()) {
      statusMap.put(status.getStatusCode(), status);
    }

    SPIServiceUtils.getAllService(StatusType.class).forEach(this::addStatusType);
  }

  public void addStatusType(StatusType status) {
    if (statusMap.containsKey(status.getStatusCode())) {
      throw new IllegalStateException("repeated status code: " + status.getStatusCode());
    }

    statusMap.put(status.getStatusCode(), status);
  }

  public StatusType getOrCreateByStatusCode(int code) {
    StatusType statusType = statusMap.get(code);
    if (statusType != null) {
      return statusType;
    }

    statusType = new HttpStatus(code, "");
    addStatusType(statusType);

    return statusType;
  }
}
