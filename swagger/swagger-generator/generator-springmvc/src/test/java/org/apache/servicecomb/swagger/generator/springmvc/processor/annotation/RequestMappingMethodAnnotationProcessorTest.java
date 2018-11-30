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

import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.models.Operation;

public class RequestMappingMethodAnnotationProcessorTest {
  @Test
  public void testProcess() throws NoSuchMethodException {
    RequestMappingMethodAnnotationProcessor processor = new RequestMappingMethodAnnotationProcessor();
    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(null, null);
    OperationGenerator operationGenerator = new OperationGenerator(swaggerGenerator, null);
    Operation operation = operationGenerator.getOperation();

    RequestMapping annotation = TestProducer.class.getMethod("testSingleMediaType", String.class)
        .getAnnotation(RequestMapping.class);
    processor.process(annotation, operationGenerator);
    Assert.assertThat(operation.getConsumes(), Matchers.contains(MediaType.APPLICATION_XML));
    Assert.assertThat(operation.getProduces(), Matchers.contains(MediaType.APPLICATION_XML));

    annotation = TestProducer.class.getMethod("testMultipleMediaType", String.class)
        .getAnnotation(RequestMapping.class);
    processor.process(annotation, operationGenerator);
    Assert.assertThat(operation.getConsumes(), Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));
    Assert.assertThat(operation.getProduces(), Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));

    annotation = TestProducer.class.getMethod("testBlankMediaType", String.class)
        .getAnnotation(RequestMapping.class);
    processor.process(annotation, operationGenerator);
    Assert.assertThat(operation.getConsumes(), Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));
    Assert.assertThat(operation.getProduces(), Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));

    operation.addConsumes(MediaType.APPLICATION_XML);
    operation.addProduces(MediaType.APPLICATION_XML);
    Assert.assertThat(operation.getConsumes(),
        Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML));
    Assert.assertThat(operation.getProduces(),
        Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML));
  }

  static class TestProducer {
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML, produces = MediaType.APPLICATION_XML)
    public String testSingleMediaType(String input) {
      return input;
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = {MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON},
        produces = {MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public String testMultipleMediaType(String input) {
      return input;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "", produces = "")
    public String testBlankMediaType(String input) {
      return input;
    }
  }
}