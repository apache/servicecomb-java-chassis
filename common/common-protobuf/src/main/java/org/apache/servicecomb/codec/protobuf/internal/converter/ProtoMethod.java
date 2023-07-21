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
package org.apache.servicecomb.codec.protobuf.internal.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.swagger.invocation.context.HttpStatus;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.ws.rs.core.Response.Status;

public class ProtoMethod {
  private String argTypeName;

  @JsonProperty
  // key is status
  private final Map<Integer, ProtoResponse> responses = new HashMap<>();

  private ProtoResponse defaultResponse;

  public String getArgTypeName() {
    return argTypeName;
  }

  public void setArgTypeName(String argTypeName) {
    this.argTypeName = argTypeName;
  }

  public void addResponse(String status, ProtoResponse response) {
    if (status.equals("default")) {
      defaultResponse = response;
      return;
    }

    int statusCode = Integer.parseInt(status);
    responses.put(statusCode, response);

    if (defaultResponse == null && statusCode == Status.OK.getStatusCode()) {
      defaultResponse = response;
    }
  }

  public ProtoResponse findResponse(int statusCode) {
    ProtoResponse response = responses.get(statusCode);
    if (response != null) {
      return response;
    }

    if (statusCode == Status.OK.getStatusCode()) {
      for (Entry<Integer, ProtoResponse> code : responses.entrySet()) {
        if (HttpStatus.isSuccess(code.getKey())) {
          return responses.get(code.getKey());
        }
      }
    }

    if (HttpStatus.isSuccess(statusCode)) {
      return responses.get(Status.OK.getStatusCode());
    }

    return defaultResponse;
  }
}
