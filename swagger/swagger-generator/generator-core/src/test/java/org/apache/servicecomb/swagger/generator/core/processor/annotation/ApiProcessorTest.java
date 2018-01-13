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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.junit.Test;
import org.mockito.Mockito;

import io.swagger.annotations.Api;

public class ApiProcessorTest {
  private ApiProcessor apiProcessor = new ApiProcessor();

  @Test
  public void process() {
    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(Mockito.mock(SwaggerGeneratorContext.class),
        null);
    apiProcessor.process(SwaggerTestTarget.class.getAnnotation(Api.class),
        swaggerGenerator);

    assertThat(swaggerGenerator.getDefaultTags(), contains("tag1", "tag2"));
  }

  @Test
  public void processOnNoTag() {
    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(Mockito.mock(SwaggerGeneratorContext.class),
        null);
    apiProcessor.process(SwaggerTestTargetWithNoTag.class.getAnnotation(Api.class), swaggerGenerator);

    Set<String> tags = swaggerGenerator.getDefaultTags();
    assertEquals(0, tags.size());
  }

  @Api(tags = {"tag1", "tag2", "", "tag1"})
  private class SwaggerTestTarget {
  }

  @Api
  private class SwaggerTestTargetWithNoTag {
  }
}
