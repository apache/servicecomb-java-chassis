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

package org.apache.servicecomb.common.rest.codec.param;

import org.apache.servicecomb.common.rest.codec.param.HeaderProcessorCreator.HeaderProcessor;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;

public class TestHeaderProcessorCreator {
  static Environment environment = Mockito.mock(Environment.class);

  @BeforeAll
  public static void beforeClass() {
    LegacyPropertyFactory.setEnvironment(environment);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.header.ignoreRequiredCheck", boolean.class, false))
        .thenReturn(false);
  }

  @Test
  public void testCreate() {
    ParamValueProcessorCreator creator =
        ParamValueProcessorCreatorManager.INSTANCE.findValue(HeaderProcessorCreator.PARAMTYPE);
    HeaderParameter hp = new HeaderParameter();
    hp.setName("h1");
    hp.setSchema(new Schema());

    ParamValueProcessor processor = creator.create(null, hp.getName(), hp, String.class);

    Assertions.assertEquals(HeaderProcessor.class, processor.getClass());
  }
}
