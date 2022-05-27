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

package org.apache.servicecomb.swagger.invocation.exception;

import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.swagger.invocation.context.HttpStatus;

/**
 * 业务在provider端，想返回在swagger中定义好的错误
 * 通过抛出本类型的异常来实现
 * 如果不是本类型的异常，则强制认为是PRODUCER_INNER_STATUS_CODE错误
 */
public class InvocationException extends RuntimeException {
  private static final long serialVersionUID = 8027482777502649656L;

  /**
   * http header中的statusCode
   * 不直接使用Status类型，是为了支持业务自定义code
   */
  private final StatusType status;

  private final Object errorData;

  public InvocationException(StatusType status, Object errorData) {
    this.status = status;
    this.errorData = errorData;
  }

  public InvocationException(StatusType status, String msg) {
    this(status, new CommonExceptionData(msg));
  }

  public InvocationException(int statusCode, String reasonPhrase, Object errorData, Throwable cause) {
    this(new HttpStatus(statusCode, reasonPhrase), errorData, cause);
  }

  public InvocationException(int statusCode, String reasonPhrase, Object errorData) {
    this(new HttpStatus(statusCode, reasonPhrase), errorData);
  }

  public InvocationException(StatusType status, Object errorData, Throwable cause) {
    super(cause);
    this.status = status;
    this.errorData = errorData;
  }

  public InvocationException(StatusType status, String code, String msg) {
    this(status, new CommonExceptionData(code, msg));
  }

  public InvocationException(StatusType status, String code, String msg, Throwable cause) {
    this(status, new CommonExceptionData(code, msg), cause);
  }

  public StatusType getStatus() {
    return status;
  }

  public int getStatusCode() {
    return status.getStatusCode();
  }

  public String getReasonPhrase() {
    return status.getReasonPhrase();
  }

  public Object getErrorData() {
    return errorData;
  }

  @SuppressWarnings("unchecked")
  public <T> T getError() {
    return (T) errorData;
  }

  @Override
  public String getMessage() {
    return this.toString();
  }

  @Override
  public String toString() {
    return "InvocationException: code=" + getStatusCode()
        + ";msg=" + getErrorData();
  }
}
