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

package org.apache.servicecomb.swagger.generator.springmvc.processor.annotation;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import mockit.Mock;
import mockit.MockUp;

public class RequestMappingClassAnnotationProcessorTest {

  private RequestMappingClassAnnotationProcessor processor = new RequestMappingClassAnnotationProcessor();

  @Test
  public void process() {
    SwaggerGenerator swaggerGenerator = getSwaggerGenerator();
    processor.process(SwaggerTestTarget.class.getAnnotation(RequestMapping.class), swaggerGenerator);

    Assert.assertEquals("/test", swaggerGenerator.getSwagger().getBasePath());
    Assert.assertEquals("post", swaggerGenerator.getHttpMethod());
    Assert.assertThat(swaggerGenerator.getSwagger().getConsumes(),
        Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));
    Assert.assertThat(swaggerGenerator.getSwagger().getProduces(),
        Matchers.contains(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
  }

  /**
   * {@link RequestMapping#value()} takes higher priority than {@link RequestMapping#path()}
   */
  @Test
  public void process_ValueOverWritePath() {
    SwaggerGenerator swaggerGenerator = getSwaggerGenerator();
    processor.process(SwaggerTestTarget_ValueOverWritePath.class.getAnnotation(RequestMapping.class), swaggerGenerator);

    Assert.assertEquals("/testValue", swaggerGenerator.getSwagger().getBasePath());
  }

  /**
   * {@link RequestMapping} takes higher priority than {@link Api} on setting {@code consumes} and {@code produces}
   */
  @Test
  public void process_OverWriteConsumesAndProduces() {
    SwaggerGenerator swaggerGenerator = getSwaggerGenerator();
    swaggerGenerator.getSwagger().addConsumes(MediaType.APPLICATION_XML);
    swaggerGenerator.getSwagger().addProduces(MediaType.APPLICATION_XML);
    processor.process(SwaggerTestTarget.class.getAnnotation(RequestMapping.class), swaggerGenerator);

    Assert.assertThat(swaggerGenerator.getSwagger().getConsumes(),
        Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));
    Assert.assertThat(swaggerGenerator.getSwagger().getProduces(),
        Matchers.contains(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
  }

  private SwaggerGenerator getSwaggerGenerator() {
    SwaggerGeneratorContext swaggerGeneratorContext = new MockUp<SwaggerGeneratorContext>() {
      @Mock
      String resolveStringValue(String strVal) {
        return strVal;
      }
    }.getMockInstance();

    return new SwaggerGenerator(swaggerGeneratorContext, null);
  }

  @RequestMapping(path = "/test", method = RequestMethod.POST,
      consumes = {MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON},
      produces = {MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  private class SwaggerTestTarget {
  }

  @RequestMapping(value = "/testValue", path = "/testPath")
  private class SwaggerTestTarget_ValueOverWritePath {
  }
}
