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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

import io.swagger.models.properties.Property;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperations;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import io.swagger.annotations.ApiOperation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

public class ApiOperationProcessorTest {
  static SwaggerOperations swaggerOperations = SwaggerOperations.generate(TestClass.class);

  @AfterAll
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

    @ApiOperation(value = "testBodyParam")
    public String testBodyParam(@RequestBody TestBodyBean user) {
      return user.toString();
    }
  }


  private static class TestBodyBean {

    @NotBlank
    private String age;

    @NotNull
    private String name;

    @NotEmpty
    private String sexes;

    public String getAge() {
      return age;
    }

    public void setAge(String age) {
      this.age = age;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getSexes() {
      return sexes;
    }

    public void setSexes(String sexes) {
      this.sexes = sexes;
    }
  }

  @Test
  public void testConvertTags() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("function");
    MatcherAssert.assertThat(swaggerOperation.getOperation().getTags(), containsInAnyOrder("tag1", "tag2"));
  }

  @Test
  public void testConvertTagsOnMethodWithNoTag() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("functionWithNoTag");
    Assertions.assertNull(swaggerOperation.getOperation().getTags());
  }

  @Test
  public void testMediaType() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testSingleMediaType");
    MatcherAssert.assertThat(swaggerOperation.getOperation().getConsumes(), Matchers.contains(MediaType.TEXT_PLAIN));
    MatcherAssert.assertThat(swaggerOperation.getOperation().getProduces(), Matchers.contains(MediaType.APPLICATION_XML));

    swaggerOperation = swaggerOperations.findOperation("testMultiMediaType");
    MatcherAssert.assertThat(swaggerOperation.getOperation().getConsumes(),
        Matchers.contains(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
    MatcherAssert.assertThat(swaggerOperation.getOperation().getProduces(),
        Matchers.contains(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML));

    swaggerOperation = swaggerOperations.findOperation("testBlankMediaType");
    Assertions.assertNull(swaggerOperation.getOperation().getConsumes());
    Assertions.assertNull(swaggerOperation.getOperation().getProduces());

    swaggerOperation.getOperation().addConsumes(MediaType.TEXT_HTML);
    swaggerOperation.getOperation().addProduces(MediaType.TEXT_HTML);
    MatcherAssert.assertThat(swaggerOperation.getOperation().getConsumes(), Matchers.contains(MediaType.TEXT_HTML));
    MatcherAssert.assertThat(swaggerOperation.getOperation().getProduces(), Matchers.contains(MediaType.TEXT_HTML));
  }


  @Test
  public void testBodyParam() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testBodyParam");
    Map<String, Property> properties = swaggerOperation.getSwagger().getDefinitions().get("TestBodyBean").getProperties();
    Assertions.assertTrue(properties.get("age").getRequired(), "Support NotBlank annotation");
    Assertions.assertTrue(properties.get("sexes").getRequired(), "Support NotEmpty annotation");
    Assertions.assertTrue(properties.get("name").getRequired(), "Original support NotNull annotation");
  }

}
