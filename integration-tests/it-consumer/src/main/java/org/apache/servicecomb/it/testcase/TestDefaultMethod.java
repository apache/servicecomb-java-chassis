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
package org.apache.servicecomb.it.testcase;

import static org.junit.Assert.assertEquals;

import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.junit.Test;

import io.swagger.models.Swagger;

public class TestDefaultMethod {
  interface DataTypePojoIntf {
    default int intBodyWithDefault() {
      return intBody(100);
    }

    int intBody(int input);
  }

  private static Consumers<DataTypePojoIntf> consumersPojo = new Consumers<>("dataTypePojo",
      DataTypePojoIntf.class);

  @Test
  public void should_support_default_method() {
    assertEquals(100, consumersPojo.getIntf().intBodyWithDefault());
  }

  @Test
  public void should_generate_swagger_without_default_method() {
    Swagger swagger = SwaggerGenerator.generate(DataTypePojoIntf.class);
    assertEquals("---\n"
            + "swagger: \"2.0\"\n"
            + "info:\n"
            + "  version: \"1.0.0\"\n"
            + "  title: \"swagger definition for org.apache.servicecomb.it.testcase.TestDefaultMethod$DataTypePojoIntf\"\n"
            + "  x-java-interface: \"org.apache.servicecomb.it.testcase.TestDefaultMethod$DataTypePojoIntf\"\n"
            + "basePath: \"/DataTypePojoIntf\"\n"
            + "consumes:\n"
            + "- \"application/json\"\n"
            + "produces:\n"
            + "- \"application/json\"\n"
            + "paths:\n"
            + "  /intBody:\n"
            + "    post:\n"
            + "      operationId: \"intBody\"\n"
            + "      parameters:\n"
            + "      - in: \"body\"\n"
            + "        name: \"input\"\n"
            + "        required: false\n"
            + "        schema:\n"
            + "          $ref: \"#/definitions/intBodyBody\"\n"
            + "      responses:\n"
            + "        \"200\":\n"
            + "          description: \"response of 200\"\n"
            + "          schema:\n"
            + "            type: \"integer\"\n"
            + "            format: \"int32\"\n"
            + "definitions:\n"
            + "  intBodyBody:\n"
            + "    type: \"object\"\n"
            + "    properties:\n"
            + "      input:\n"
            + "        type: \"integer\"\n"
            + "        format: \"int32\"\n",
        SwaggerUtils.swaggerToString(swagger));
  }
}
