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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperations;
import org.hamcrest.Matchers;
import org.junit.Test;

import io.swagger.annotations.Api;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.models.Swagger;

public class ApiProcessorTest {
  @Api(tags = {"tag1", "tag2", "", "tag1"})
  private class SwaggerTestTarget {
    public void op() {

    }
  }

  @Api
  private class SwaggerTestTargetWithNoTag {
    public void op() {

    }
  }

  @SwaggerDefinition(consumes = {"", " "}, produces = {"", " "})
  @Api(consumes = MediaType.TEXT_PLAIN + " , " + MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_XML + "," + MediaType.APPLICATION_JSON)
  private class OverrideEmptyConsumesAndProduces {
  }

  @SwaggerDefinition(consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.MULTIPART_FORM_DATA)
  @Api(consumes = MediaType.TEXT_PLAIN + " , " + MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_XML + "," + MediaType.APPLICATION_JSON)
  private class OverWriteValidConsumesAndProduces {
  }

  @Api(consumes = MediaType.TEXT_PLAIN + " , " + MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_XML + "," + MediaType.APPLICATION_JSON)
  private class pureApi {
  }

  @Test
  public void process() {
    SwaggerOperations swaggerOperations = SwaggerOperations.generate(SwaggerTestTarget.class);
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("op");

    assertThat(swaggerOperation.getOperation().getTags(), contains("tag1", "tag2"));
    assertThat(swaggerOperation.getSwagger().getConsumes(), Matchers.contains(MediaType.APPLICATION_JSON));
    assertThat(swaggerOperation.getSwagger().getProduces(), Matchers.contains(MediaType.APPLICATION_JSON));
  }

  @Test
  public void processOnNoTag() {
    SwaggerOperations swaggerOperations = SwaggerOperations.generate(SwaggerTestTargetWithNoTag.class);
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("op");

    assertNull(swaggerOperation.getOperation().getTags());
    assertThat(swaggerOperation.getSwagger().getConsumes(), Matchers.contains(MediaType.APPLICATION_JSON));
    assertThat(swaggerOperation.getSwagger().getProduces(), Matchers.contains(MediaType.APPLICATION_JSON));
  }

  @Test
  public void processOverWriteEmptyConsumesAndProduces() {
    Swagger swagger = SwaggerGenerator.generate(OverrideEmptyConsumesAndProduces.class);

    assertThat(swagger.getConsumes(), Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));
    assertThat(swagger.getProduces(), Matchers.contains(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON));
  }

  @Test
  public void processNotOverWriteValidConsumesAndProduces() {
    Swagger swagger = SwaggerGenerator.generate(OverWriteValidConsumesAndProduces.class);

    assertThat(swagger.getConsumes(), Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));
    assertThat(swagger.getProduces(), Matchers.contains(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON));
  }

  @Test
  public void processWithConsumesAndProduces() {
    Swagger swagger = SwaggerGenerator.generate(pureApi.class);

    assertThat(swagger.getConsumes(), Matchers.contains(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));
    assertThat(swagger.getProduces(), Matchers.contains(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON));
  }
}
