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

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiOperation;

@RestSchema(schemaId = "apiOperationSpringmvcSchemaTest")
@RequestMapping(path = "v1/apiOperationSpringmvcHello")
public class ApiOperationSpringmvcSchema {

  @RequestMapping(path = "/sayHi", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
  @ApiOperation(value = "", nickname = "sayHi")
  public String sayHello(@RequestParam("index") Long index) {
    return "ApiOperationSpringmvcSchema#sayHello" + index;
  }

  @RequestMapping(path = "/sayHello", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
  @ApiOperation(value = "", nickname = "sayHello")
  public String sayHello(String name) {
    return name;
  }
}
