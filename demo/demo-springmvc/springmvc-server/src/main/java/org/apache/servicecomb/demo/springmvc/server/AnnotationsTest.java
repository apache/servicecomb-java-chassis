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

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.demo.controller.Person;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiParam;

@RestSchema(schemaId = "annotations")
@RequestMapping(path = "/springmvc/annotations", produces = MediaType.APPLICATION_JSON)
public class AnnotationsTest {
  @GetMapping(path = "/add")
  public int add(@RequestParam(name = "a", defaultValue = "10") int a,
      @RequestParam(name = "b", defaultValue = "10") int b) {
    return a + b;
  }

  @RequestMapping(path = "/sayhei", method = RequestMethod.GET)
  public String sayHei(@RequestHeader(name = "name", defaultValue = "test") String name) {
    return "hei " + name;
  }

  @GetMapping(path = "/sayhi")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "name", paramType = "query", dataType = "string", defaultValue = "test"),
      @ApiImplicitParam(name = "age", paramType = "query", dataType = "integer", defaultValue = "20")
  })
  public String sayHi(String name, int age) {
    return "hi " + name + " your age is : " + age;
  }

  @RequestMapping(path = "/saysomething", method = RequestMethod.POST)
  public String saySomething(String prefix, @RequestBody(required = false) @ApiParam(required = false) Person user) {
    if (user == null || user.getName() == null || user.getName().isEmpty()) {
      return "No user data found";
    }
    return prefix + " " + user.getName();
  }

  @RequestMapping(path = "/say", method = RequestMethod.POST)
  public String say(@RequestBody(required = false) String user) {
    if (user == null || user.isEmpty()) {
      return "No user name found";
    }
    return user;
  }
}
