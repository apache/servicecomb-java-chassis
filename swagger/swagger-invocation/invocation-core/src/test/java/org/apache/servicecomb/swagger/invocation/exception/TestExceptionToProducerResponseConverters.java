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

import java.io.IOException;
import java.util.Arrays;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class TestExceptionToProducerResponseConverters {
  @Test
  public void convertExceptionToResponse(
      @Mocked ExceptionToProducerResponseConverter<Throwable> c1,
      @Mocked Response r1,
      @Mocked ExceptionToProducerResponseConverter<Throwable> c2,
      @Mocked Response r2,
      @Mocked ExceptionToProducerResponseConverter<Throwable> cDef) {
    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getSortedService(ExceptionToProducerResponseConverter.class);
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
      }
    };

    ExceptionToProducerResponseConverters exceptionToProducerResponseConverters = new ExceptionToProducerResponseConverters();

    Assert.assertSame(r1,
        exceptionToProducerResponseConverters.convertExceptionToResponse(null, new Throwable()));
    Assert.assertSame(r2,
        exceptionToProducerResponseConverters.convertExceptionToResponse(null, new Exception()));
    Assert.assertSame(r2,
        exceptionToProducerResponseConverters.convertExceptionToResponse(null,
            new IllegalStateException()));
  }

  @Test
  public void convertExceptionToResponse_checkDefaultConverterPriority(
      @Mocked ExceptionToProducerResponseConverter<Throwable> c1,
      @Mocked Response r1,
      @Mocked ExceptionToProducerResponseConverter<Throwable> c2,
      @Mocked Response r2,
      @Mocked ExceptionToProducerResponseConverter<Throwable> cDef,
      @Mocked Response rDef,
      @Mocked ExceptionToProducerResponseConverter<Throwable> cDef2) {
    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getSortedService(ExceptionToProducerResponseConverter.class);
        result = Arrays.asList(c1, c2, cDef, cDef2);

        c1.getExceptionClass();
        result = RuntimeException.class;
        c1.convert((SwaggerInvocation) any, (Throwable) any);
        result = r1;

        c2.getExceptionClass();
        result = InvocationException.class;
        c2.convert((SwaggerInvocation) any, (Throwable) any);
        result = r2;

        cDef.getExceptionClass();
        result = null;
        cDef.convert((SwaggerInvocation) any, (Throwable) any);
        result = rDef;

        cDef2.getExceptionClass();
        result = null;
      }
    };

    ExceptionToProducerResponseConverters exceptionToProducerResponseConverters = new ExceptionToProducerResponseConverters();

    Assert.assertSame(r2,
        exceptionToProducerResponseConverters
            .convertExceptionToResponse(null, new InvocationException(Status.UNAUTHORIZED, "")));
    Assert.assertSame(r1,
        exceptionToProducerResponseConverters.convertExceptionToResponse(null, new RuntimeException()));
    Assert.assertSame(rDef,
        exceptionToProducerResponseConverters.convertExceptionToResponse(null,
            new IOException()));
  }

  @Test
  public void convertExceptionToResponse_CheckCommonConvertPriority(
      @Mocked ExceptionToProducerResponseConverter<RuntimeException0> cR0,
      @Mocked ExceptionToProducerResponseConverter<RuntimeException0> cR0_LowPriority,
      @Mocked ExceptionToProducerResponseConverter<RuntimeException1> cR1,
      @Mocked ExceptionToProducerResponseConverter<RuntimeException> cR,
      @Mocked ExceptionToProducerResponseConverter<Throwable> cT,
      @Mocked ExceptionToProducerResponseConverter<?> cDef,
      @Mocked Response rR0,
      @Mocked Response rR1,
      @Mocked Response rR,
      @Mocked Response rT) {
    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getSortedService(ExceptionToProducerResponseConverter.class);
        result = Arrays.asList(cR, cR0, cR0_LowPriority, cR1, cDef, cT);

        cR0.getExceptionClass();
        result = RuntimeException0.class;
        cR0.convert((SwaggerInvocation) any, (RuntimeException0) any);
        result = rR0;

        cR0_LowPriority.getExceptionClass();
        result = RuntimeException0.class;

        cR1.getExceptionClass();
        result = RuntimeException1.class;
        cR1.convert((SwaggerInvocation) any, (RuntimeException1) any);
        result = rR1;

        cR.getExceptionClass();
        result = RuntimeException.class;
        cR.convert((SwaggerInvocation) any, (RuntimeException) any);
        result = rR;

        cT.getExceptionClass();
        result = Throwable.class;
        cT.convert((SwaggerInvocation) any, (Throwable) any);
        result = rT;

        cDef.getExceptionClass();
        result = null;
      }
    };

    ExceptionToProducerResponseConverters exceptionToProducerResponseConverters = new ExceptionToProducerResponseConverters();

    Assert.assertSame(rR0,
        exceptionToProducerResponseConverters.convertExceptionToResponse(null, new RuntimeException0_0()));
    Assert.assertSame(rR0,
        exceptionToProducerResponseConverters.convertExceptionToResponse(null, new RuntimeException0()));
    Assert.assertSame(rR1,
        exceptionToProducerResponseConverters.convertExceptionToResponse(null, new RuntimeException1()));
    Assert.assertSame(rR,
        exceptionToProducerResponseConverters.convertExceptionToResponse(null, new RuntimeException()));
    // Actually, a Throwable exception converter will act like a default converter, as our implementation expects.
    Assert.assertSame(rT,
        exceptionToProducerResponseConverters.convertExceptionToResponse(null, new IOException()));
  }

  static class RuntimeException0 extends RuntimeException {
    private static final long serialVersionUID = -5151948381107463505L;
  }

  static class RuntimeException1 extends RuntimeException {
    private static final long serialVersionUID = 1752513688353075486L;
  }

  static class RuntimeException0_0 extends RuntimeException0 {
    private static final long serialVersionUID = -6645187961518504765L;
  }
}
