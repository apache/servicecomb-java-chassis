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

import java.util.Arrays;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class TestExceptionToResponseConverters {
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void convertExceptionToResponse(@Mocked ExceptionToResponseConverter c1,
      @Mocked Response r1,
      @Mocked ExceptionToResponseConverter c2,
      @Mocked Response r2,
      @Mocked ExceptionToResponseConverter cDef,
      @Mocked Response rDef) {
    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getAllService(ExceptionToResponseConverter.class);
        result = Arrays.asList(c1, c2, cDef);

        c1.getExceptionClass();
        result = Throwable.class;
        c1.convert((SwaggerInvocation) any, (Throwable) any);
        result = r1;

        c2.getExceptionClass();
        result = Exception.class;
        c2.convert((SwaggerInvocation) any, (Throwable) any);
        result = r2;

        cDef.getExceptionClass();
        result = null;
        cDef.convert((SwaggerInvocation) any, (Throwable) any);
        result = rDef;
      }
    };

    ExceptionToResponseConverters exceptionToResponseConverters = new ExceptionToResponseConverters();

    Assert.assertSame(r1,
        exceptionToResponseConverters.convertExceptionToResponse((SwaggerInvocation) null, new Throwable()));
    Assert.assertSame(r2,
        exceptionToResponseConverters.convertExceptionToResponse((SwaggerInvocation) null, new Exception()));
    Assert.assertSame(rDef,
        exceptionToResponseConverters.convertExceptionToResponse((SwaggerInvocation) null,
            new IllegalStateException()));
  }
}
