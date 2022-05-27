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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionToProducerResponseConverters {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionToProducerResponseConverters.class);

  private final Map<Class<?>, ExceptionToProducerResponseConverter<Throwable>> exceptionToProducerResponseConverters =
      new HashMap<>();

  private ExceptionToProducerResponseConverter<Throwable> defaultConverter;

  /**
   * Load the {@link ExceptionToProducerResponseConverter} implementations. Ensure that those converters whose {@link ExceptionToProducerResponseConverter#getOrder()}
   * return smaller value takes higher priority.
   */
  @SuppressWarnings("unchecked")
  public ExceptionToProducerResponseConverters() {
    SPIServiceUtils.getSortedService(ExceptionToProducerResponseConverter.class).forEach(converter -> {
      if (converter.getExceptionClass() == null) {
        if (defaultConverter == null) {
          defaultConverter = converter;
        }
        return;
      }

      exceptionToProducerResponseConverters.putIfAbsent(converter.getExceptionClass(), converter);
    });
  }

  public Response convertExceptionToResponse(SwaggerInvocation swaggerInvocation, Throwable e) {
    ExceptionToProducerResponseConverter<Throwable> converter = null;
    Class<?> clazz = e.getClass();
    while (converter == null) {
      converter = exceptionToProducerResponseConverters.get(clazz);
      if (clazz == Throwable.class) {
        break;
      }
      clazz = clazz.getSuperclass();
    }
    if (converter == null) {
      converter = defaultConverter;
    }
    try {
      return converter.convert(swaggerInvocation, e);
    } catch (Throwable throwable) {
      // In case users do not implement correctly and maybe discovered at runtime to cause asycResponse callback hang.
      LOGGER
          .error("ExceptionToProducerResponseConverter {} cannot throw exception, please fix it.", converter.getClass(),
              throwable);
      return Response.failResp(swaggerInvocation.getInvocationType(), e);
    }
  }
}
