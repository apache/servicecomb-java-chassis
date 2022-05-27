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

import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;

/**
 *  ExceptionToProducerResponseConverter are used to convert provider Exceptions to user friendly messages.
 *  They are called when :
 *  1. exception happens executing business logic
 *  2. exception happens in ProducerInvokeExtension.beforeMethodInvoke(e.g. parameter validation)
 */
public interface ExceptionToProducerResponseConverter<T extends Throwable> {
  Class<T> getExceptionClass();

  default int getOrder() {
    return 0;
  }

  Response convert(SwaggerInvocation swaggerInvocation, T e);
}
