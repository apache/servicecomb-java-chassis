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

package org.apache.servicecomb.swagger.generator.core.processor.annotation;

import static org.hamcrest.Matchers.contains;

import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperations;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;

public class ApiProcessorTest {
  @OpenAPIDefinition(tags = {@Tag(name = "tag1"), @Tag(name = "tag2"), @Tag(name = ""), @Tag(name = "tag1")})
  private static class SwaggerTestTarget {
    public void op() {

    }
  }

  @OpenAPIDefinition
  private static class SwaggerTestTargetWithNoTag {
    public void op() {

    }
  }

  @Test
  public void process() {
    SwaggerOperations swaggerOperations = SwaggerOperations.generate(SwaggerTestTarget.class);
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("op");

    MatcherAssert.assertThat(swaggerOperation.getOperation().getTags(), contains("tag1", "tag2"));
  }

  @Test
  public void processOnNoTag() {
    SwaggerOperations swaggerOperations = SwaggerOperations.generate(SwaggerTestTargetWithNoTag.class);
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("op");

    Assertions.assertNull(swaggerOperation.getOperation().getTags());
  }
}
