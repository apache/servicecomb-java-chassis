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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperations;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Test;

import io.swagger.annotations.ApiOperation;

public class ApiOperationProcessorTest {
  static SwaggerOperations swaggerOperations = SwaggerOperations.generate(TestClass.class);

  @AfterClass
  public static void teardown() {
    swaggerOperations = null;
  }

  private static class TestClass {
    @ApiOperation(value = "value1", tags = {"tag1", "tag2"})
    public void function() {
    }

    @ApiOperation(value = "value2")
    public void functionWithNoTag() {
    }

    @ApiOperation(value = "testSingleMediaType", consumes = MediaType.TEXT_PLAIN, produces = MediaType.APPLICATION_XML)
    public String testSingleMediaType(String input) {
      return input;
    }

    @ApiOperation(value = "testMultiMediaType",
        consumes = MediaType.APPLICATION_JSON + "," + MediaType.TEXT_PLAIN,
        produces = MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML)
    public String testMultiMediaType(String input) {
      return input;
    }

    @ApiOperation(value = "testBlankMediaType", consumes = "", produces = "")
    public String testBlankMediaType(String input) {
      return input;
    }
  }

  @Test
  public void testConvertTags() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("function");
    assertThat(swaggerOperation.getOperation().getTags(), containsInAnyOrder("tag1", "tag2"));
  }

  @Test
  public void testConvertTagsOnMethodWithNoTag() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("functionWithNoTag");
    assertNull(swaggerOperation.getOperation().getTags());
  }

  @Test
  public void testMediaType() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testSingleMediaType");
    assertThat(swaggerOperation.getOperation().getConsumes(), Matchers.contains(MediaType.TEXT_PLAIN));
    assertThat(swaggerOperation.getOperation().getProduces(), Matchers.contains(MediaType.APPLICATION_XML));

    swaggerOperation = swaggerOperations.findOperation("testMultiMediaType");
    assertThat(swaggerOperation.getOperation().getConsumes(),
        Matchers.contains(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
    assertThat(swaggerOperation.getOperation().getProduces(),
        Matchers.contains(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML));

    swaggerOperation = swaggerOperations.findOperation("testBlankMediaType");
    assertNull(swaggerOperation.getOperation().getConsumes());
    assertNull(swaggerOperation.getOperation().getProduces());

    swaggerOperation.getOperation().addConsumes(MediaType.TEXT_HTML);
    swaggerOperation.getOperation().addProduces(MediaType.TEXT_HTML);
    assertThat(swaggerOperation.getOperation().getConsumes(), Matchers.contains(MediaType.TEXT_HTML));
    assertThat(swaggerOperation.getOperation().getProduces(), Matchers.contains(MediaType.TEXT_HTML));
  }
}
