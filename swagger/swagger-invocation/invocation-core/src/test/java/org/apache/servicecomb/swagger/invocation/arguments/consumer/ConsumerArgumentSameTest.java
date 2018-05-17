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

package org.apache.servicecomb.swagger.invocation.arguments.consumer;

import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.converter.Converter;
import org.junit.Test;
import org.mockito.Mockito;

public class ConsumerArgumentSameTest {

  private Converter mockConverter = Mockito.mock(Converter.class);

  private ConsumerArgumentSame consumerArgumentSame = new ConsumerArgumentSame(0, 0, mockConverter);

  @Test
  public void testMapArgumentOnArgument() {
    SwaggerInvocation swaggerInvocation = Mockito.mock(SwaggerInvocation.class);
    String[] args = {"testArg"};

    Mockito.when(mockConverter.convert(args[0])).thenReturn(args[0]);

    consumerArgumentSame.mapArgument(swaggerInvocation, args);

    Mockito.verify(mockConverter, Mockito.times(1)).convert(args[0]);
    Mockito.verify(swaggerInvocation, Mockito.times(1)).setSwaggerArgument(0, args[0]);
  }

  @Test
  public void testMapArgumentOnArgumentIsNull() {
    SwaggerInvocation swaggerInvocation = Mockito.mock(SwaggerInvocation.class);

    consumerArgumentSame.mapArgument(swaggerInvocation, new Object[1]);

    Mockito.verify(mockConverter, Mockito.never()).convert(Mockito.anyObject());
    Mockito.verify(swaggerInvocation, Mockito.never()).setSwaggerArgument(Mockito.anyInt(), Mockito.anyObject());
  }
}
