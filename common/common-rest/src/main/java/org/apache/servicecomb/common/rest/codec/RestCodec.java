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

package org.apache.servicecomb.common.rest.codec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.definition.RestParam;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response.Status;

public final class RestCodec {
  private RestCodec() {
  }

  public static void argsToRest(Map<String, Object> args, RestOperationMeta restOperation,
      RestClientRequest clientRequest) throws Exception {
    int paramSize = restOperation.getParamList().size();
    if (paramSize == 0) {
      return;
    }

    for (int idx = 0; idx < paramSize; idx++) {
      RestParam param = restOperation.getParamList().get(idx);
      param.getParamProcessor().setValue(clientRequest, args.get(param.getParamName()));
    }
  }

  public static Map<String, Object> restToArgs(HttpServletRequest request,
      RestOperationMeta restOperation) throws InvocationException {
    List<RestParam> paramList = restOperation.getParamList();

    Map<String, Object> paramValues = new HashMap<>();
    for (RestParam param : paramList) {
      try {
        paramValues.put(param.getParamName(), param.getParamProcessor().getValue(request));
      } catch (Exception e) {
        String message = String
            .format("Parameter is not valid for operation [%s]. Parameter is [%s]. Processor is [%s]. Message is [%s].",
                restOperation.getOperationMeta().getMicroserviceQualifiedName(),
                param.getParamName(),
                param.getParamProcessor().getProcessorType(),
                e.getMessage());
        throw new InvocationException(Status.BAD_REQUEST, new CommonExceptionData(message), e);
      }
    }

    return paramValues;
  }
}
