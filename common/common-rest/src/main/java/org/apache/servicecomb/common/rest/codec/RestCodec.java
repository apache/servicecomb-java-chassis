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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.definition.RestParam;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

public final class RestCodec {
  private static final Logger LOG = LoggerFactory.getLogger(RestCodec.class);

  private RestCodec() {
  }

  public static void argsToRest(Object[] args, RestOperationMeta restOperation,
      RestClientRequest clientRequest) throws Exception {
    int paramSize = restOperation.getParamList().size();
    if (paramSize == 0) {
      return;
    }

    if (paramSize != args.length) {
      throw new Exception("wrong number of arguments");
    }

    for (int idx = 0; idx < paramSize; idx++) {
      RestParam param = restOperation.getParamList().get(idx);
      param.getParamProcessor().setValue(clientRequest, args[idx]);
    }
  }

  public static Object[] restToArgs(HttpServletRequest request,
      RestOperationMeta restOperation) throws InvocationException {
    List<RestParam> paramList = restOperation.getParamList();

    Object[] paramValues = new Object[paramList.size()];
    for (int idx = 0; idx < paramList.size(); idx++) {
      RestParam param = paramList.get(idx);
      try {
        paramValues[idx] = param.getParamProcessor().getValue(request);
      } catch (Exception e) {
        // Avoid information leak of user input, and add option for debug use.
        String message = String.format("Parameter is not valid for operation [%s]. Parameter is [%s]. Processor is [%s].",
            restOperation.getOperationMeta().getMicroserviceQualifiedName(),
            param.getParamName(),
            param.getParamProcessor().getProcessorType());
        if (DynamicPropertyFactory.getInstance().getBooleanProperty(
            RestConst.PRINT_CODEC_ERROR_MESSGAGE, false).get()) {
          LOG.error(message, e);
          throw new InvocationException(Status.BAD_REQUEST.getStatusCode(), "", message, e);
        } else {
          LOG.error("{} Add {}=true to print the details.", message, RestConst.PRINT_CODEC_ERROR_MESSGAGE);
          throw new InvocationException(Status.BAD_REQUEST, message);
        }
      }
    }

    return paramValues;
  }
}
