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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import io.swagger.annotations.Api;

public class ApiProcessorTest {
  private ApiProcessor apiProcessor = new ApiProcessor();

  @Test
  public void process() {
    SwaggerGenerator swaggerGenerator = getSwaggerGenerator();
    apiProcessor.process(SwaggerTestTarget.class.getAnnotation(Api.class),
        swaggerGenerator);

    assertThat(swaggerGenerator.getDefaultTags(), contains("tag1", "tag2"));
    assertNull(swaggerGenerator.getSwagger().getConsumes());
    assertNull(swaggerGenerator.getSwagger().getProduces());
  }

  @Test
  public void processOnNoTag() {
    SwaggerGenerator swaggerGenerator = getSwaggerGenerator();
    apiProcessor.process(SwaggerTestTargetWithNoTag.class.getAnnotation(Api.class), swaggerGenerator);

    Set<String> tags = swaggerGenerator.getDefaultTags();
    assertEquals(0, tags.size());
    assertNull(swaggerGenerator.getSwagger().getConsumes());
    assertNull(swaggerGenerator.getSwagger().getProduces());
  }

  @Test
  public void processOverWriteEmptyConsumesAndProduces() {
    SwaggerGenerator swaggerGenerator = getSwaggerGenerator();
    swaggerGenerator.getSwagger().setConsumes(Arrays.asList("", "  "));
    swaggerGenerator.getSwagger().setProduces(Arrays.asList("", "  "));
    apiProcessor.process(SwaggerTestTargetWithConsumesAndProduces.class.getAnnotation(Api.class), swaggerGenerator);

    List<String> consumes = swaggerGenerator.getSwagger().getConsumes();
    assertThat(consumes, Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));
    List<String> produces = swaggerGenerator.getSwagger().getProduces();
    assertThat(produces, Matchers.contains(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON));
  }

  @Test
  public void processNotOverWriteValidConsumesAndProduces() {
    SwaggerGenerator swaggerGenerator = getSwaggerGenerator();
    swaggerGenerator.getSwagger().setConsumes(Collections.singletonList(MediaType.MULTIPART_FORM_DATA));
    swaggerGenerator.getSwagger().setProduces(Collections.singletonList(MediaType.MULTIPART_FORM_DATA));
    apiProcessor.process(SwaggerTestTargetWithConsumesAndProduces.class.getAnnotation(Api.class), swaggerGenerator);

    List<String> consumes = swaggerGenerator.getSwagger().getConsumes();
    assertThat(consumes, Matchers.contains(MediaType.MULTIPART_FORM_DATA));
    List<String> produces = swaggerGenerator.getSwagger().getProduces();
    assertThat(produces, Matchers.contains(MediaType.MULTIPART_FORM_DATA));
  }

  @Test
  public void processWithConsumesAndProduces() {
    SwaggerGenerator swaggerGenerator = getSwaggerGenerator();
    apiProcessor.process(SwaggerTestTargetWithConsumesAndProduces.class.getAnnotation(Api.class), swaggerGenerator);

    List<String> consumes = swaggerGenerator.getSwagger().getConsumes();
    assertThat(consumes, Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));
    List<String> produces = swaggerGenerator.getSwagger().getProduces();
    assertThat(produces, Matchers.contains(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON));
  }

  private SwaggerGenerator getSwaggerGenerator() {
    return new SwaggerGenerator(Mockito.mock(SwaggerGeneratorContext.class),
        null);
  }

  @Api(tags = {"tag1", "tag2", "", "tag1"})
  private class SwaggerTestTarget {
  }

  @Api
  private class SwaggerTestTargetWithNoTag {
  }

  @Api(consumes = MediaType.TEXT_PLAIN + " , " + MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_XML + "," + MediaType.APPLICATION_JSON)
  private class SwaggerTestTargetWithConsumesAndProduces {
  }
}
