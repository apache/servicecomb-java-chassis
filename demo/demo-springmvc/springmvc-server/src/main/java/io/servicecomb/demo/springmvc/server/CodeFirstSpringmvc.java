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

package io.servicecomb.demo.springmvc.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.servo.monitor.Monitor;

import io.servicecomb.common.rest.codec.RestObjectMapper;
import io.servicecomb.demo.compute.Person;
import io.servicecomb.demo.ignore.InputModelForTestIgnore;
import io.servicecomb.demo.ignore.OutputModelForTestIgnore;
import io.servicecomb.demo.server.User;
import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.foundation.metrics.MetricsServoRegistry;
import io.servicecomb.provider.rest.common.RestSchema;
import io.servicecomb.swagger.extend.annotations.RawJsonRequestBody;
import io.servicecomb.swagger.extend.annotations.RequestParamColFmt;
import io.servicecomb.swagger.extend.annotations.ResponseHeaders;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.context.ContextUtils;
import io.servicecomb.swagger.invocation.context.InvocationContext;
import io.servicecomb.swagger.invocation.exception.InvocationException;
import io.servicecomb.swagger.invocation.response.Headers;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import io.vertx.core.json.JsonObject;

@RestSchema(schemaId = "codeFirst")
@RequestMapping(path = "/codeFirstSpringmvc", produces = MediaType.APPLICATION_JSON_VALUE)
public class CodeFirstSpringmvc {

  private static final Logger LOGGER= LoggerFactory.getLogger(CodeFirstSpringmvc.class);
  private MetricsServoRegistry registry;

  @Autowired
  public CodeFirstSpringmvc(MetricsServoRegistry registry) {
    this.registry = registry;
  }


  private String _fileUpload(MultipartFile file1, Part file2) {
    try (InputStream is1 = file1.getInputStream(); InputStream is2 = file2.getInputStream()) {
      String content1 = IOUtils.toString(is1);
      String content2 = IOUtils.toString(is2);
      return String.format("%s:%s:%s\n"
          + "%s:%s:%s",
          file1.getOriginalFilename(),
          file1.getContentType(),
          content1,
          file2.getSubmittedFileName(),
          file2.getContentType(),
          content2);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @PostMapping(path = "/upload", produces = MediaType.TEXT_PLAIN_VALUE)
  public String fileUpload(@RequestPart(name = "file1") MultipartFile file1,
      @RequestPart(name = "someFile") Part file2) {
    return _fileUpload(file1, file2);
  }

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

  @PostMapping(path = "/testUserMap")
  public Map<String, User> testUserMap(@RequestBody Map<String, User> userMap) {
    return userMap;
  }

  @RequestMapping(path = "/testUserMapGeneric", method = RequestMethod.POST)
  public TemplateResponse<String> testUserMapGeneric(@RequestBody Map<String, User> userMap) {
    return null;
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

  // this should be ignored as it's hidden
  @ApiOperation(value = "", hidden = true, httpMethod = "POST")
  public int add(@RequestParam("a") int a) {
    return a;
  }

  @RequestMapping(path = "/add", method = RequestMethod.POST)
  public int add(@RequestAttribute("a") int a, @RequestAttribute("b") int b) {
    return a + b;
  }

  @GetMapping(path = "/reduce")
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

  @PutMapping(path = "/sayhi/{name}")
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

  @DeleteMapping(path = "/addstring", produces = MediaType.TEXT_PLAIN_VALUE)
  public String addString(@RequestParam(name = "s") List<String> s) {
    String result = "";
    for (String x : s) {
      result += x;
    }
    return result;
  }

  @RequestMapping(path = "/fallback/returnnull/{name}", method = RequestMethod.GET)
  @ApiResponses(value = {@ApiResponse(code = 200, response = String.class, message = "xxx"),
      @ApiResponse(code = 490, response = String.class, message = "xxx")})
  public String fallbackReturnNull(@PathVariable(name = "name") String name) {
    if ("throwexception".equals(name)) {
      throw new InvocationException(490, "490", "xxx");
    }
    return name;
  }

  @RequestMapping(path = "/fallback/throwexception/{name}", method = RequestMethod.GET)
  @ApiResponses(value = {@ApiResponse(code = 200, response = String.class, message = "xxx"),
      @ApiResponse(code = 490, response = String.class, message = "xxx")})
  public String fallbackThrowException(@PathVariable(name = "name") String name) {
    if ("throwexception".equals(name)) {
      throw new InvocationException(490, "490", "xxx");
    }
    return name;
  }

  @RequestMapping(path = "/fallback/fromcache/{name}", method = RequestMethod.GET)
  @ApiResponses(value = {@ApiResponse(code = 200, response = String.class, message = "xxx"),
      @ApiResponse(code = 490, response = String.class, message = "xxx")})
  public String fallbackFromCache(@PathVariable(name = "name") String name) {
    if ("throwexception".equals(name)) {
      throw new InvocationException(490, "490", "xxx");
    }
    return name;
  }

  @RequestMapping(path = "/fallback/force/{name}", method = RequestMethod.GET)
  @ApiResponses(value = {@ApiResponse(code = 200, response = String.class, message = "xxx"),
      @ApiResponse(code = 490, response = String.class, message = "xxx")})
  public String fallbackForce(@PathVariable(name = "name") String name) {
    if ("throwexception".equals(name)) {
      throw new InvocationException(490, "490", "xxx");
    }
    return name;
  }

  enum NameType {
    abc,
    def
  }

  @RequestMapping(path = "/testenum/{name}", method = RequestMethod.GET)
  @ApiResponses(value = {@ApiResponse(code = 200, response = String.class, message = "200 normal"),
      @ApiResponse(code = 490, response = String.class, message = "490 exception")})
  public String testEnum(@RequestParam(name = "username") String username,
      @PathVariable(value = "name") NameType nameType) {
    return nameType.toString();
  }

  @RequestMapping(method = RequestMethod.POST, value = "/ignore")
  @ResponseBody
  public OutputModelForTestIgnore testModelWithIgnoreField(@RequestBody InputModelForTestIgnore input) {
    return new OutputModelForTestIgnore("output_id", input.getInputId(), input.getContent(), input.getInputObject(),
        input.getInputJsonObject(), input.getInputIgnoreInterface(),
        new Person("outputSomeone"), new JsonObject("{\"OutputJsonKey\" : \"OutputJsonValue\"}"), () -> {
        });
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(method = RequestMethod.POST, value = "/rawJsonAnnotation")
  @ResponseBody
  public String testRawJsonAnnotation(@RawJsonRequestBody String jsonInput) {
    Map<String, String> person;
    try {
      person = RestObjectMapper.INSTANCE.readValue(jsonInput.getBytes(), Map.class);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    return "hello " + person.get("name");
  }

  @PostMapping(path = "/testform")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "form1", dataType = "string", paramType = "form", value = "a required form param",
          required = true),
      @ApiImplicitParam(name = "form2", dataType = "string", paramType = "form", value = "an optional form param",
          required = false)})
  public String testform(HttpServletRequest request) {
    String form1 = request.getParameter("form1");
    String form2 = request.getParameter("form2");
    Assert.notNull(form1);
    return form1 + form2;
  }

  //Only for 0.5.0 Integration Test
  @RequestMapping(path = "/metricsForTest", method = RequestMethod.GET)
  public String metricsForTest() {
    List<Monitor<?>> monitors = registry.getMetricsMonitors();
    Map<String, String> values = new HashMap<>();
    for (Monitor<?> monitor : monitors) {
      values.put(monitor.getConfig().getName(), monitor.getValue().toString());
    }
    try {
      return JsonUtils.writeValueAsString(values);
    } catch (JsonProcessingException e) {
      throw new InvocationException(500, "500", "JsonProcessingException", e);
    }
  }

  @RequestMapping(path = "/testGetStrArray", method = RequestMethod.GET)
  public String[] testGetStrArray(@RequestParamColFmt(collectionFormat = "csv") String[] str) {
    for (int i = 0; i < str.length; i++) {
      LOGGER.info("*******"+str[i]);
    }
    return str;
  }
}
