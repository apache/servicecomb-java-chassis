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

import org.apache.servicecomb.foundation.common.DynamicObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 将普通异常转换为InvocationException时，保存message信息
 */
public class CommonExceptionData extends DynamicObject {
  @JsonInclude(Include.NON_NULL)
  private String code;

  private String message;

  public CommonExceptionData() {
  }

  public CommonExceptionData(String message) {
    this.message = message;
  }

  public CommonExceptionData(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public CommonExceptionData setCode(String code) {
    this.code = code;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public CommonExceptionData setMessage(String message) {
    this.message = message;
    return this;
  }

  @Override
  public String toString() {
    if (code == null) {
      return "CommonExceptionData [message=" + message + "]";
    }

    return "CommonExceptionData{" +
        "code='" + code + '\'' +
        ", message='" + message + '\'' +
        ", dynamic=" + dynamic +
        '}';
  }
}
