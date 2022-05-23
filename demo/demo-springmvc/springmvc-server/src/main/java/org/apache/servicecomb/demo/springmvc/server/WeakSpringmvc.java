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

import java.util.List;

import org.apache.servicecomb.demo.model.SpecialNameModel;
import org.apache.servicecomb.demo.server.GenericsModel;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiOperation;

@RestSchema(schemaId = "weakSpringmvc")
@RequestMapping(path = "/weakSpringmvc", produces = MediaType.APPLICATION_JSON_VALUE)
public class WeakSpringmvc {
  @GetMapping(path = "/diffNames")
  @ApiOperation(value = "differentName", nickname = "differentName")
  public int diffNames(@RequestParam("x") int a, @RequestParam("y") int b) {
    return a * 2 + b;
  }

  @GetMapping(path = "/genericParams")
  @ApiOperation(value = "genericParams", nickname = "genericParams")
  public List<List<String>> genericParams(@RequestParam("code") int code, @RequestBody List<List<String>> names) {
    return names;
  }

  @GetMapping(path = "/genericParamsModel")
  @ApiOperation(value = "genericParamsModel", nickname = "genericParamsModel")
  public GenericsModel genericParamsModel(@RequestParam("code") int code, @RequestBody GenericsModel model) {
    return model;
  }

  @GetMapping(path = "/specialNameModel")
  @ApiOperation(value = "specialNameModel", nickname = "specialNameModel")
  public SpecialNameModel specialNameModel(@RequestParam("code") int code, @RequestBody SpecialNameModel model) {
    return model;
  }

}
