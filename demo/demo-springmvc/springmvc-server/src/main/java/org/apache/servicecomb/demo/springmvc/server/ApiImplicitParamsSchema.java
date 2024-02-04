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
package org.apache.servicecomb.demo.springmvc.server;

import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

@RestSchema(schemaId = "ApiImplicitParamsSchema")
@RequestMapping(path = "/implicit", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiImplicitParams({
    @ApiImplicitParam(name = "x-test-a", paramType = "header", dataType = "string", defaultValue = "test"),
    @ApiImplicitParam(name = "x-test-b", paramType = "header", dataType = "integer", defaultValue = "20", required = true)
})
public class ApiImplicitParamsSchema {
  @GetMapping(path = "/testImplicitAndExplicitParam")
  public String testImplicitAndExplicitParam(HttpServletRequest request,
      @RequestParam("a") int a, @RequestParam("b") int b) {
    return request.getHeader("x-test-a") + "," + request.getHeader("x-test-b") + "," +
        (a + b);
  }

  @GetMapping(path = "/testIntegerTypeValidation")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "x-test-c", paramType = "header", dataType = "integer")
  })
  public String testIntegerTypeValidation(HttpServletRequest request) {
    return request.getHeader("x-test-a") + "," + request.getHeader("x-test-b") + "," +
        request.getHeader("x-test-c");
  }
}
