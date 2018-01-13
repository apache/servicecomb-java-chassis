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

package org.apache.servicecomb.core.exception;

import org.apache.servicecomb.foundation.common.RegisterManager;
import org.apache.servicecomb.foundation.common.utils.FortifyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionUtils.class);

  // 异常码
  private static final String ERROR_DESC_MGR_MSG = "error desc mgr";

  protected static final RegisterManager<String, String> ERROR_DESC_MGR = new RegisterManager<>(ERROR_DESC_MGR_MSG);

  private static final String CSE_SCHEMA_OPERATION_ID_INVALID = "cse.schema.operation.id.invalid";

  private static final String CSE_HANDLER_REF_NOT_EXIST = "cse.handler.ref.not.exist";

  private static final String CSE_PRODUCER_OPERATION_NOT_EXIST = "cse.producer.operation.not.exist";

  private static final String CSE_LB_NO_AVAILABLE_ADDRESS = "cse.lb.no.available.address";

  static {
    ERROR_DESC_MGR.register(CSE_HANDLER_REF_NOT_EXIST, "Handler not exist, id=%s");
    ERROR_DESC_MGR.register(CSE_SCHEMA_OPERATION_ID_INVALID, "OperationId is invalid, schemaId=%s, path=%s");
    ERROR_DESC_MGR.register(CSE_PRODUCER_OPERATION_NOT_EXIST,
        "Producer operation not exist, schemaId=%s, operationName=%s");
    ERROR_DESC_MGR.register(CSE_LB_NO_AVAILABLE_ADDRESS,
        "No available address found. microserviceName=%s, version=%s, discoveryGroupName=%s");
  }

  protected ExceptionUtils() {
  }

  // TODO：应该改为protected，不允许随便调，所有异常，都必须是强类型的
  public static CseException createCseException(String code, Object... args) {
    String msg = String.format(ERROR_DESC_MGR.ensureFindValue(code), args);

    CseException exception = new CseException(code, msg);
    LOGGER.error(FortifyUtils.getErrorInfo(exception));
    return exception;
  }

  public static CseException createCseException(String code, Throwable cause, Object... args) {
    String msg = String.format(ERROR_DESC_MGR.ensureFindValue(code), args);

    CseException exception = new CseException(code, msg, cause);
    LOGGER.error(FortifyUtils.getErrorInfo(exception));
    return exception;
  }

  public static CseException producerOperationNotExist(String schemaId, String operationName) {
    return createCseException(CSE_PRODUCER_OPERATION_NOT_EXIST,
        schemaId,
        operationName);
  }

  public static CseException operationIdInvalid(String schemaId, String path) {
    return createCseException(CSE_SCHEMA_OPERATION_ID_INVALID,
        schemaId,
        path);
  }

  public static CseException handlerRefNotExist(String id) {
    return createCseException(CSE_HANDLER_REF_NOT_EXIST, id);
  }

  public static CseException lbAddressNotFound(String microserviceName, String microserviceVersionRule,
      String discoveryGroupName) {
    return createCseException(CSE_LB_NO_AVAILABLE_ADDRESS,
        microserviceName,
        microserviceVersionRule,
        discoveryGroupName);
  }
}
