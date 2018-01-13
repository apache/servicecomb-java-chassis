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

package org.apache.servicecomb.demo.springmvc.tests.endpoints;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;

import org.apache.servicecomb.demo.controller.Person;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.extend.annotations.ResponseHeaders;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ResponseHeader;

@Profile("SimplifiedMapping")
@RestSchema(schemaId = "codeFirstSpringmvcSimplifiedMappingAnnotation")
@RequestMapping(path = "/codeFirstSpringmvc", produces = MediaType.APPLICATION_JSON_VALUE)
public class CodeFirstSpringmvcSimplifiedMappingAnnotation extends CodeFirstSpringmvcBase {
  @ResponseHeaders({@ResponseHeader(name = "h1", response = String.class),
      @ResponseHeader(name = "h2", response = String.class)})
  @PostMapping(path = "/responseEntity")
  @Override
  public ResponseEntity<Date> responseEntity(InvocationContext c1, @RequestAttribute("date") Date date) {
    return super.responseEntity(c1, date);
  }

  @ApiResponse(code = 200, response = User.class, message = "")
  @ResponseHeaders({@ResponseHeader(name = "h1", response = String.class),
      @ResponseHeader(name = "h2", response = String.class)})
  @GetMapping(path = "/cseResponse")
  @Override
  public Response cseResponse(InvocationContext c1) {
    return super.cseResponse(c1);
  }

  @PostMapping(path = "/testUserMap")
  @Override
  public Map<String, User> testUserMap(@RequestBody Map<String, User> userMap) {
    return super.testUserMap(userMap);
  }

  @PostMapping(path = "/textPlain", consumes = MediaType.TEXT_PLAIN_VALUE)
  @Override
  public String textPlain(@RequestBody String body) {
    return super.textPlain(body);
  }

  @PostMapping(path = "/bytes")
  @Override
  public byte[] bytes(@RequestBody byte[] input) {
    return super.bytes(input);
  }

  @PostMapping(path = "/upload", produces = MediaType.TEXT_PLAIN_VALUE)
  @Override
  public String fileUpload(@RequestPart(name = "file1") MultipartFile file1,
      @RequestPart(name = "someFile") MultipartFile file2, @RequestAttribute("name") String name) {
    return super.fileUpload(file1, file2, name);
  }

  @PostMapping(path = "/uploadWithoutAnnotation", produces = MediaType.TEXT_PLAIN_VALUE)
  public String fileUploadWithoutAnnotation(MultipartFile file1, MultipartFile file2,
      @RequestAttribute("name") String name) {
    return super.fileUpload(file1, file2, name);
  }

  @PostMapping(path = "/addDate")
  @Override
  public Date addDate(@RequestAttribute("date") Date date, @QueryParam("seconds") long seconds) {
    return super.addDate(date, seconds);
  }


  // this should be ignored as it's hidden
  @ApiOperation(value = "", hidden = true, httpMethod = "POST")
  public int add(@RequestParam("a") int a) {
    return a;
  }

  @PostMapping(path = "/add")
  @Override
  public int add(@RequestAttribute("a") int a, @RequestAttribute("b") int b) {
    return super.add(a, b);
  }

  @GetMapping(path = "/reduce")
  @ApiImplicitParams({@ApiImplicitParam(name = "a", dataType = "integer", format = "int32", paramType = "query")})
  @Override
  public int reduce(HttpServletRequest request, @CookieValue(name = "b") int b) {
    return super.reduce(request, b);
  }

  @PostMapping(path = "/sayhello")
  @Override
  public Person sayHello(@RequestBody Person user) {
    return super.sayHello(user);
  }

  @PostMapping(path = "/testrawjson")
  @Override
  public String testRawJsonString(String jsonInput) {
    return super.testRawJsonString(jsonInput);
  }

  @PostMapping(path = "/saysomething")
  @Override
  public String saySomething(@RequestHeader(name = "prefix") String prefix, @RequestBody Person user) {
    return super.saySomething(prefix, user);
  }

  @PutMapping(path = "/sayhi/{name}")
  @Override
  public String sayHi(@PathVariable(name = "name") String name) {
    return super.sayHi(name);
  }

  @PutMapping(path = "/sayhi/{name}/v2")
  @Override
  public String sayHi2(@PathVariable(name = "name") String name) {
    return super.sayHi2(name);
  }

  @GetMapping(path = "/istrue")
  @Override
  public boolean isTrue() {
    return super.isTrue();
  }

  @DeleteMapping(path = "/addstring", produces = MediaType.TEXT_PLAIN_VALUE)
  @Override
  public String addString(@RequestParam(name = "s") List<String> s) {
    return super.addString(s);
  }
}
