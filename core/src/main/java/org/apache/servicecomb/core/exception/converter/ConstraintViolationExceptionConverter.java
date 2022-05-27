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
package org.apache.servicecomb.core.exception.converter;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.apache.servicecomb.core.exception.ExceptionCodes.DEFAULT_VALIDATE;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.ExceptionConverter;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

public class ConstraintViolationExceptionConverter implements ExceptionConverter<ConstraintViolationException> {
  public static final int ORDER = Short.MAX_VALUE;

  public static final String KEY_CODE = "servicecomb.filters.validate.code";

  private DynamicStringProperty code;

  public ConstraintViolationExceptionConverter() {
    refreshCode();
  }

  /**
   * during UT, DynamicPropertyFactory will be reset, this caused code can not changed by event
   */
  @VisibleForTesting
  public void refreshCode() {
    code = DynamicPropertyFactory.getInstance().getStringProperty(KEY_CODE, DEFAULT_VALIDATE);
  }

  @Override
  public int getOrder() {
    return ORDER;
  }

  @Override
  public boolean canConvert(Throwable throwable) {
    return throwable instanceof ConstraintViolationException;
  }

  @Override
  public InvocationException convert(Invocation invocation, ConstraintViolationException throwable,
      StatusType genericStatus) {
    List<ValidateDetail> details = throwable.getConstraintViolations().stream()
        .map(violation -> new ValidateDetail(violation.getPropertyPath().toString(), violation.getMessage()))
        .collect(Collectors.toList());

    CommonExceptionData exceptionData = new CommonExceptionData(code.get(), "invalid parameters.");
    exceptionData.putDynamic("validateDetail", details);
    return new InvocationException(BAD_REQUEST, exceptionData);
  }
}
