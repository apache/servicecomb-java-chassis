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

package org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation;

import java.lang.annotation.Annotation;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.swagger.models.Operation;

public class ConsumesAnnotationProcessorTest {
  @Test
  public void testProcess() throws NoSuchMethodException {
    ConsumesAnnotationProcessor processor = new ConsumesAnnotationProcessor();
    Operation operation = new Operation();
    OperationGenerator operationGenerator = mockOperationGenerator(operation);

    Annotation consumesAnnotation = TestProducer.class.getMethod("testSingleMediaType", String.class)
        .getAnnotation(Consumes.class);
    processor.process(consumesAnnotation, operationGenerator);
    Assert.assertThat(operation.getConsumes(), Matchers.contains(MediaType.APPLICATION_JSON));

    consumesAnnotation = TestProducer.class.getMethod("testMultipleMediaType", String.class)
        .getAnnotation(Consumes.class);
    processor.process(consumesAnnotation, operationGenerator);
    Assert.assertThat(operation.getConsumes(), Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));

    consumesAnnotation = TestProducer.class.getMethod("testBlankMediaType", String.class)
        .getAnnotation(Consumes.class);
    processor.process(consumesAnnotation, operationGenerator);
    Assert.assertThat(operation.getConsumes(), Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));

    operation.addConsumes(MediaType.APPLICATION_XML);
    Assert.assertThat(operation.getConsumes(),
        Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML));
  }

  static OperationGenerator mockOperationGenerator(Operation operation) {
    OperationGenerator operationGenerator = Mockito.mock(OperationGenerator.class);
    Mockito.when(operationGenerator.getOperation()).thenReturn(operation);
    return operationGenerator;
  }

  static class TestProducer {
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_XML)
    public String testSingleMediaType(String input) {
      return input;
    }

    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    public String testMultipleMediaType(String input) {
      return input;
    }

    @Consumes("")
    @Produces("")
    public String testBlankMediaType(String input) {
      return input;
    }
  }
}