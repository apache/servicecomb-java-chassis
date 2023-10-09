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

import static jakarta.ws.rs.core.Response.Status.Family.CLIENT_ERROR;
import static org.apache.servicecomb.core.exception.ExceptionCodes.GENERIC_CLIENT;
import static org.apache.servicecomb.core.exception.ExceptionCodes.GENERIC_SERVER;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.utils.SPIOrder;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import jakarta.ws.rs.core.Response.StatusType;

public interface ExceptionConverter<T extends Throwable> extends SPIOrder {
  static boolean isClient(StatusType status) {
    return CLIENT_ERROR.equals(status.getFamily());
  }

  static String getGenericCode(StatusType status) {
    return isClient(status) ? GENERIC_CLIENT : GENERIC_SERVER;
  }

  /**
   *
   * @param throwable exception will be converted
   * @return can convert the exception
   */
  boolean canConvert(Throwable throwable);

  /**
   *
   * @param invocation related invocation, will be null only when failed to prepare invocation
   * @param throwable exception will be converted
   * @param genericStatus if can not determine the status type, then use this input value
   * @return converted invocation exception
   */
  InvocationException convert(Invocation invocation, T throwable, StatusType genericStatus);
}
