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

import mockit.Mocked;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class TestDefaultExceptionToProducerResponseConverter {
  DefaultExceptionToProducerResponseConverter converter = new DefaultExceptionToProducerResponseConverter();

  @Test
  public void getExceptionClass() {
    Assertions.assertNull(converter.getExceptionClass());
  }

  @Test
  public void convert(@Mocked SwaggerInvocation swaggerInvocation) {
    IllegalStateException stateException = new IllegalStateException("test");
    Response response = converter.convert(swaggerInvocation, stateException);
    Assertions.assertSame(stateException, ((InvocationException) response.getResult()).getCause());
  }
}
