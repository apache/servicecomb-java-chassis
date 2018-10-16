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

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation.ConsumesAnnotationProcessorTest.TestProducer;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.models.Operation;

public class ProducesAnnotationProcessorTest {
  @Test
  public void testProcess() throws NoSuchMethodException {
    ProducesAnnotationProcessor processor = new ProducesAnnotationProcessor();
    Operation operation = new Operation();
    OperationGenerator operationGenerator = ConsumesAnnotationProcessorTest.mockOperationGenerator(operation);

    Annotation produceAnnotation = TestProducer.class.getMethod("testSingleMediaType", String.class)
        .getAnnotation(Produces.class);
    processor.process(produceAnnotation, operationGenerator);
    Assert.assertThat(operation.getProduces(), Matchers.contains(MediaType.APPLICATION_XML));

    produceAnnotation = TestProducer.class.getMethod("testMultipleMediaType", String.class)
        .getAnnotation(Produces.class);
    processor.process(produceAnnotation, operationGenerator);
    Assert.assertThat(operation.getProduces(), Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML));

    produceAnnotation = TestProducer.class.getMethod("testBlankMediaType", String.class)
        .getAnnotation(Produces.class);
    processor.process(produceAnnotation, operationGenerator);
    Assert.assertThat(operation.getProduces(), Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML));

    operation.addProduces(MediaType.APPLICATION_JSON);
    Assert.assertThat(operation.getProduces(),
        Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON));
  }
}