/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.demo.springmvc.server;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response.Status;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.servicecomb.common.rest.codec.RestObjectMapper;
import io.servicecomb.demo.compute.Person;
import io.servicecomb.demo.server.User;
import io.servicecomb.provider.rest.common.RestSchema;
import io.servicecomb.swagger.extend.annotations.ResponseHeaders;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.context.ContextUtils;
import io.servicecomb.swagger.invocation.context.InvocationContext;
import io.servicecomb.swagger.invocation.response.Headers;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ResponseHeader;

@RestSchema(schemaId = "codeFirst")
@RequestMapping(path = "/codeFirstSpringmvc", produces = MediaType.APPLICATION_JSON_VALUE)
public class CodeFirstSpringmvc {
  @ResponseHeaders({@ResponseHeader(name = "h1", response = String.class),
      @ResponseHeader(name = "h2", response = String.class)})
  @RequestMapping(path = "/responseEntity", method = RequestMethod.POST)
  public ResponseEntity<Date> responseEntity(InvocationContext c1, @RequestAttribute("date") Date date) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("h1", "h1v " + c1.getContext().toString());

    InvocationContext c2 = ContextUtils.getInvocationContext();
    headers.add("h2", "h2v " + c2.getContext().toString());

    return new ResponseEntity<Date>(date, headers, HttpStatus.ACCEPTED);
  }

  @ResponseHeaders({@ResponseHeader(name = "h1", response = String.class),
      @ResponseHeader(name = "h2", response = String.class)})
  @RequestMapping(path = "/responseEntity", method = RequestMethod.PATCH)
  public ResponseEntity<Date> responseEntityPATCH(InvocationContext c1, @RequestAttribute("date") Date date) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("h1", "h1v " + c1.getContext().toString());

    InvocationContext c2 = ContextUtils.getInvocationContext();
    headers.add("h2", "h2v " + c2.getContext().toString());

    return new ResponseEntity<Date>(date, headers, HttpStatus.ACCEPTED);
  }

  @ApiResponse(code = 200, response = User.class, message = "")
  @ResponseHeaders({@ResponseHeader(name = "h1", response = String.class),
      @ResponseHeader(name = "h2", response = String.class)})
  @RequestMapping(path = "/cseResponse", method = RequestMethod.GET)
  public Response cseResponse(InvocationContext c1) {
    Response response = Response.createSuccess(Status.ACCEPTED, new User());
    Headers headers = response.getHeaders();
    headers.addHeader("h1", "h1v " + c1.getContext().toString());

    InvocationContext c2 = ContextUtils.getInvocationContext();
    headers.addHeader("h2", "h2v " + c2.getContext().toString());

    return response;
  }

  @RequestMapping(path = "/testUserMap", method = RequestMethod.POST)
  public Map<String, User> testUserMap(@RequestBody Map<String, User> userMap) {
    return userMap;
  }

  @RequestMapping(path = "/textPlain", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
  public String textPlain(@RequestBody String body) {
    return body;
  }

  @RequestMapping(path = "/bytes", method = RequestMethod.POST)
  public byte[] bytes(@RequestBody byte[] input) {
    input[0] = (byte) (input[0] + 1);
    return input;
  }

  @RequestMapping(path = "/addDate", method = RequestMethod.POST)
  public Date addDate(@RequestAttribute("date") Date date, @QueryParam("seconds") long seconds) {
    return new Date(date.getTime() + seconds * 1000);
  }

  @RequestMapping(path = "/add", method = RequestMethod.POST)
  public int add(@RequestAttribute("a") int a, @RequestAttribute("b") int b) {
    return a + b;
  }

  @RequestMapping(path = "/reduce", method = RequestMethod.GET)
  @ApiImplicitParams({@ApiImplicitParam(name = "a", dataType = "integer", format = "int32", paramType = "query")})
  public int reduce(HttpServletRequest request, @CookieValue(name = "b") int b) {
    int a = Integer.parseInt(request.getParameter("a"));
    return a - b;
  }

  @RequestMapping(path = "/sayhello", method = RequestMethod.POST)
  public Person sayHello(@RequestBody Person user) {
    user.setName("hello " + user.getName());
    return user;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(path = "/testrawjson", method = RequestMethod.POST)
  public String testRawJsonString(String jsonInput) {
    Map<String, String> person;
    try {
      person = RestObjectMapper.INSTANCE.readValue(jsonInput.getBytes(), Map.class);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    return "hello " + person.get("name");
  }

  @RequestMapping(path = "/saysomething", method = RequestMethod.POST)
  public String saySomething(@RequestHeader(name = "prefix") String prefix, @RequestBody Person user) {
    return prefix + " " + user.getName();
  }

  @RequestMapping(path = "/sayhi/{name}", method = RequestMethod.PUT)
  public String sayHi(@PathVariable(name = "name") String name) {
    ContextUtils.getInvocationContext().setStatus(202);
    return name + " sayhi";
  }

  @RequestMapping(path = "/sayhi/{name}/v2", method = RequestMethod.PUT)
  public String sayHi2(@PathVariable(name = "name") String name) {
    return name + " sayhi 2";
  }

  @RequestMapping(path = "/istrue", method = RequestMethod.GET)
  public boolean isTrue() {
    return true;
  }

  @RequestMapping(path = "/addstring", method = RequestMethod.DELETE, produces = MediaType.TEXT_PLAIN_VALUE)
  public String addString(@RequestParam(name = "s") List<String> s) {
    String result = "";
    for (String x : s) {
      result += x;
    }
    return result;
  }
}
