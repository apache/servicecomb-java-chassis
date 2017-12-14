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

package io.servicecomb.engine;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import io.servicecomb.swagger.engine.SwaggerEnvironment;
import io.servicecomb.swagger.engine.SwaggerProducer;
import io.servicecomb.swagger.engine.bootstrap.BootstrapNormal;
import io.servicecomb.swagger.invocation.models.ProducerImpl;

public class TestSwaggerEnvironment {
  private static SwaggerEnvironment env;

  private static SwaggerProducer producer;

  @BeforeClass
  public static void init() {
    env = new BootstrapNormal().boot();
    producer = env.createProducer(new ProducerImpl());
  }

  @Test
  public void ableToFindVisibleMethod() {
    assertThat(producer.findOperation("visibleMethod"), is(notNullValue()));
  }

  @Test
  public void unableToFindHiddenMethod() {
    assertThat(producer.findOperation("hiddenMethod"), is(nullValue()));
  }
}
