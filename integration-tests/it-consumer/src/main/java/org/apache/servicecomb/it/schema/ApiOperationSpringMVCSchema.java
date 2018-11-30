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

package org.apache.servicecomb.it.schema;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;

@RestSchema(schemaId = "apiOperationSpringMVCSchema")
@RequestMapping(value = "/apiOperationSpringMVCSchema", consumes = MediaType.TEXT_PLAIN, produces = MediaType.TEXT_HTML)
@SwaggerDefinition(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_XML)
public class ApiOperationSpringMVCSchema {
  @RequestMapping(value = "/testMediaType1", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN, produces = MediaType.TEXT_HTML)
  @ApiOperation(value = "testMediaType1", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_XML)
  public String testMediaType1(@RequestBody String input) {
    return input;
  }

  @ApiOperation(value = "testMediaType2", consumes = MediaType.TEXT_PLAIN, produces = MediaType.TEXT_HTML)
  @PutMapping(value = "/testMediaType2", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_XML)
  public String testMediaType2(@RequestBody String input) {
    return input;
  }
}
