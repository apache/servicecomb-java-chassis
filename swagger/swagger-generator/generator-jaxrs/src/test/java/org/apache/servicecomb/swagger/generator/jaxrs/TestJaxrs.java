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

package org.apache.servicecomb.swagger.generator.jaxrs;

import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.apache.servicecomb.swagger.generator.jaxrs.model.ConsumesAndProduces;
import org.junit.jupiter.api.Test;

public class TestJaxrs {
  @Test
  public void testMultiDefaultPath() {
    UnitTestSwaggerUtils.testException(
        "Duplicate operation path detected. method=org.apache.servicecomb.swagger.generator.jaxrs.MultiDefaultPath:p2.",
        MultiDefaultPath.class);
  }

  @Test
  public void testEcho() {
    UnitTestSwaggerUtils.testSwagger("schemas/echo.yaml", Echo.class);
  }


  @Test
  public void testClassMethodNoPath() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.jaxrs.ClassMethodNoPath:p1.",
        "Path must not both be empty in class and method",
        ClassMethodNoPath.class);
  }

  @Test
  public void testFullSwaggerService() {
    UnitTestSwaggerUtils.testSwagger("schemas/FullSwaggerService.yaml", FullSwaggerService.class);
  }

  @Test
  public void consumesAndProduces() {
    UnitTestSwaggerUtils.testSwagger("schemas/consumes.yaml", ConsumesAndProduces.class);
  }
}
