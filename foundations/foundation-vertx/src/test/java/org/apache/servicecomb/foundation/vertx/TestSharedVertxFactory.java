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
package org.apache.servicecomb.foundation.vertx;

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import io.vertx.core.file.impl.FileResolverImpl;

public class TestSharedVertxFactory {
  Environment environment = Mockito.mock(Environment.class);

  @BeforeEach
  public void setUp() {
    Mockito.when(environment.getProperty("servicecomb.transport.eventloop.size", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty(FileResolverImpl.DISABLE_CP_RESOLVING_PROP_NAME, boolean.class, true))
        .thenReturn(true);
    LegacyPropertyFactory.setEnvironment(environment);
  }


  @Test
  public void getTransportVertx() {
    Assertions.assertNotNull(SharedVertxFactory.getSharedVertx(environment));
    Assertions.assertSame(SharedVertxFactory.getSharedVertx(environment),
        SharedVertxFactory.getSharedVertx(environment));

    SharedVertxFactory.getSharedVertx(environment).close();
  }
}
