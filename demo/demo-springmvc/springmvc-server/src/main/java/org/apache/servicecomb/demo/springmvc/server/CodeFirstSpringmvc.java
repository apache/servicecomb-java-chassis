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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.demo.EmptyObject;
import org.apache.servicecomb.demo.Generic;
import org.apache.servicecomb.demo.compute.GenericParam;
import org.apache.servicecomb.demo.compute.GenericParamExtended;
import org.apache.servicecomb.demo.compute.GenericParamWithJsonIgnore;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.demo.ignore.InputModelForTestIgnore;
import org.apache.servicecomb.demo.ignore.OutputModelForTestIgnore;
import org.apache.servicecomb.demo.jaxbbean.JAXBPerson;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.demo.springmvc.decoderesponse.DecodeTestResponse;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.metrics.core.MetricsBootListener;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.extend.annotations.RawJsonRequestBody;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.vertx.core.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response.Status;

@RestSchema(schemaId = "codeFirst")
@RequestMapping(path = "/codeFirstSpringmvc", produces = MediaType.APPLICATION_JSON_VALUE)
public class CodeFirstSpringmvc {
  private static final Logger LOGGER = LoggerFactory.getLogger(CodeFirstSpringmvc.class);

  private AtomicInteger invocationCounter = new AtomicInteger(0);

  private String _fileUpload(MultipartFile file1, Part file2) {
    try (InputStream is1 = file1.getInputStream(); InputStream is2 = file2.getInputStream()) {
      String content1 = IOUtils.toString(is1, StandardCharsets.UTF_8);
      String content2 = IOUtils.toString(is2, StandardCharsets.UTF_8);
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

  @GetMapping(path = "/retrySuccess")
  public int retrySuccess(@RequestParam("a") int a, @RequestParam("b") int b) {
    if (invocationCounter.getAndIncrement() % 3 != 0) {
      throw new InvocationException(Status.SERVICE_UNAVAILABLE, "try again later.");
    }
    return a + b;
  }

  @PostMapping(path = "/upload1", produces = MediaType.TEXT_PLAIN_VALUE)
  public String fileUpload1(@RequestPart(name = "file1") MultipartFile file1) throws IOException {
    try (InputStream is = file1.getInputStream()) {
      return IOUtils.toString(is, StandardCharsets.UTF_8);
    }
  }

  @PostMapping(path = "/upload", produces = MediaType.TEXT_PLAIN_VALUE)
  public String fileUpload(@RequestPart(name = "file1") MultipartFile file1,
      @RequestPart(name = "someFile") Part file2) {
    return _fileUpload(file1, file2);
  }

  @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Date.class))
      , description = "",
      headers = {@Header(name = "h1", schema = @Schema(implementation = String.class)),
          @Header(name = "h2", schema = @Schema(implementation = String.class))})
  @RequestMapping(path = "/responseEntity", method = RequestMethod.POST)
  public ResponseEntity<Date> responseEntity(InvocationContext c1, @RequestAttribute("date") Date date) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("h1", "h1v " + c1.getContext().get(Const.SRC_MICROSERVICE));

    InvocationContext c2 = ContextUtils.getInvocationContext();
    headers.add("h2", "h2v " + c2.getContext().get(Const.SRC_MICROSERVICE));

    return new ResponseEntity<>(date, headers, HttpStatus.ACCEPTED);
  }

  @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Date.class))
      , description = "",
      headers = {@Header(name = "h1", schema = @Schema(implementation = String.class)),
          @Header(name = "h2", schema = @Schema(implementation = String.class))})
  @RequestMapping(path = "/responseEntity", method = RequestMethod.PATCH)
  public ResponseEntity<Date> responseEntityPATCH(InvocationContext c1, @RequestAttribute("date") Date date) {
    return responseEntity(c1, date);
  }

  // This definition is not correct, response type is not
  // same as the actual one. May be not support in future.
  @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = User.class))
      , description = "",
      headers = {@Header(name = "h1", schema = @Schema(implementation = String.class)),
          @Header(name = "h2", schema = @Schema(implementation = String.class))})
  @RequestMapping(path = "/cseResponse", method = RequestMethod.GET)
  public Response cseResponse(InvocationContext c1) {
    Response response = Response.createSuccess(Status.ACCEPTED, new User());
    response.addHeader("h1", "h1v " + c1.getContext().get(Const.SRC_MICROSERVICE));

    InvocationContext c2 = ContextUtils.getInvocationContext();
    response.addHeader("h2", "h2v " + c2.getContext().get(Const.SRC_MICROSERVICE));

    return response;
  }

  // This definition is correct, but not supported by highway.
  // highway do not support define code other than 200
  @ApiResponse(responseCode = "202", content = @Content(schema = @Schema(implementation = User.class))
      , description = "",
      headers = {@Header(name = "h1", schema = @Schema(implementation = String.class)),
          @Header(name = "h2", schema = @Schema(implementation = String.class))})
  @RequestMapping(path = "/cseResponseCorrect", method = RequestMethod.GET)
  public Response cseResponseCorrect(InvocationContext c1) {
    Response response = Response.createSuccess(Status.ACCEPTED, new User());
    response.addHeader("h1", "h1v " + c1.getContext().get(Const.SRC_MICROSERVICE));

    InvocationContext c2 = ContextUtils.getInvocationContext();
    response.addHeader("h2", "h2v " + c2.getContext().get(Const.SRC_MICROSERVICE));

    return response;
  }

  @PostMapping(path = "/testUserMap")
  public Map<String, User> testUserMap(@RequestBody Map<String, User> userMap) {
    return userMap;
  }

  @RequestMapping(path = "/textPlain", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
  public String textPlain(@RequestBody String body) {
    return body;
  }

  @RequestMapping(path = "/appXml", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
  public JAXBPerson appXml(@RequestBody JAXBPerson person) {
    return person;
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
  @Operation(summary = "", hidden = true, method = "POST")
  public int add(@RequestParam("a") int a) {
    return a;
  }

  @RequestMapping(path = "/add", method = RequestMethod.POST)
  public int add(@RequestAttribute("a") int a, @RequestAttribute("b") int b) {
    return a + b;
  }

  @GetMapping(path = "/reduce")
  @Parameters({@Parameter(name = "a", schema = @Schema(implementation = int.class), in = ParameterIn.QUERY)})
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
      person = RestObjectMapperFactory.getRestObjectMapper()
          .readValue(jsonInput.getBytes(StandardCharsets.UTF_8), Map.class);
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

  @RequestMapping(path = "/sayhi/compressed/{name}/v2", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
  public String sayHiForCompressed(@PathVariable(name = "name") String name) {
    String bigText =
        "This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,"
            + "This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,"
            + "This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,"
            + "This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,"
            + "This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,"
            + "This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text!";
    return name + " sayhi compressed:" + bigText;
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
    StringBuilder result = new StringBuilder();
    for (String x : s) {
      result.append(x);
    }
    return result.toString();
  }

  // Using 490, 590 error code, the response type should be CommonExceptionData. Or we need
  // complex ExceptionConverters to deal with exceptions thrown by Hanlders, etc.
  @RequestMapping(path = "/fallback/returnnull/{name}", method = RequestMethod.GET)
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      content = @Content(schema = @Schema(implementation = String.class)), description = "xxx"),
      @ApiResponse(responseCode = "490", content = @Content(schema = @Schema(implementation = CommonExceptionData.class)), description = "xxx")})
  public String fallbackReturnNull(@PathVariable(name = "name") String name) {
    if ("throwexception".equals(name)) {
      throw new InvocationException(490, "490", new CommonExceptionData("xxx"));
    }
    return name;
  }

  @RequestMapping(path = "/fallback/throwexception/{name}", method = RequestMethod.GET)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class))
          , description = "xxx"),
      @ApiResponse(responseCode = "490", content = @Content(schema = @Schema(implementation = CommonExceptionData.class))
          , description = "xxx")})
  public String fallbackThrowException(@PathVariable(name = "name") String name) {
    if ("throwexception".equals(name)) {
      throw new InvocationException(490, "490", new CommonExceptionData("xxx"));
    }
    return name;
  }

  @RequestMapping(path = "/fallback/fromcache/{name}", method = RequestMethod.GET)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class))
          , description = "xxx"),
      @ApiResponse(responseCode = "490", content = @Content(schema = @Schema(implementation = CommonExceptionData.class))
          , description = "xxx")})
  public String fallbackFromCache(@PathVariable(name = "name") String name) {
    if ("throwexception".equals(name)) {
      throw new InvocationException(490, "490", new CommonExceptionData("xxx"));
    }
    return name;
  }

  @RequestMapping(path = "/fallback/force/{name}", method = RequestMethod.GET)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class))
          , description = "xxx"),
      @ApiResponse(responseCode = "490", content = @Content(schema = @Schema(implementation = CommonExceptionData.class))
          , description = "xxx")})
  public String fallbackForce(@PathVariable(name = "name") String name) {
    if ("throwexception".equals(name)) {
      throw new InvocationException(490, "490", new CommonExceptionData("xxx"));
    }
    return name;
  }

  public enum NameType {
    abc,
    def
  }

  @RequestMapping(path = "/testenum/{name}", method = RequestMethod.GET)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class))
          , description = "xxx"),
      @ApiResponse(responseCode = "490", content = @Content(schema = @Schema(implementation = CommonExceptionData.class))
          , description = "xxx")})
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
      person = RestObjectMapperFactory.getRestObjectMapper()
          .readValue(jsonInput.getBytes(StandardCharsets.UTF_8), Map.class);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    return "hello " + person.get("name");
  }

  @PostMapping(path = "/testform")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = {@Content(mediaType = SwaggerConst.FORM_MEDIA_TYPE,
          schema = @Schema(name = "form1", implementation = String.class,
              nullable = false, description = "a required form param")),
          @Content(mediaType = SwaggerConst.FORM_MEDIA_TYPE,
              schema = @Schema(name = "form2", implementation = String.class,
                  nullable = true, description = "an optional form param"))})
  public String testform(HttpServletRequest request) {
    String form1 = request.getParameter("form1");
    String form2 = request.getParameter("form2");
    Assert.notNull(form1, "from1 is null");
    return form1 + form2;
  }

  @Autowired
  MetricsBootListener metricsBootListener;

  //Only for Prometheus integration test
  @RequestMapping(path = "/prometheusForTest", method = RequestMethod.GET)
  public String prometheusForTest() {
    // just for test, this makes client always can got latest metrics
    metricsBootListener.getMetricsBootstrap().pollMeters();

    RestTemplate defaultRestTemplate = new RestTemplate();
    return defaultRestTemplate.getForObject("http://localhost:9696/metrics", String.class);
  }

  @GetMapping(path = "/traceId")
  public String getTraceId() {
    return ContextUtils.getInvocationContext().getContext(Const.TRACE_ID_NAME);
  }

  @PostMapping(path = "/emptyObject")
  public EmptyObject testEmpty(@RequestBody EmptyObject input) {
    return input;
  }

  @PostMapping(path = "/object")
  public Object testObject(@RequestBody Object input) {
    return input;
  }

  @PostMapping(path = "/mapObject")
  public Map<String, Object> testMapObject(@RequestBody Map<String, Object> input) {
    return input;
  }

  @PostMapping(path = "/listObject")
  public List<Object> testListObject(@RequestBody List<Object> input) {
    return input;
  }

  @PostMapping(path = "/holderObject")
  public Holder<Object> testHolderObject(@RequestBody Holder<Object> input) {
    return input;
  }

  @PostMapping(path = "/holderUser")
  public Holder<User> testHolderUser(@RequestBody Holder<User> input) {
    Assert.isInstanceOf(Holder.class, input);
    Assert.isInstanceOf(User.class, input.value);
    return input;
  }

  @PostMapping(path = "/genericUser")
  public Generic<User> testGenericUser(@RequestBody Generic<User> input) {
    Assert.isInstanceOf(Generic.class, input);
    Assert.isInstanceOf(User.class, input.value);
    return input;
  }

  @PostMapping(path = "/genericLong")
  public Generic<Long> testGenericLong(@RequestBody Generic<Long> input) {
    Assert.isInstanceOf(Generic.class, input);
    Assert.isInstanceOf(Long.class, input.value);
    return input;
  }

  @PostMapping(path = "/genericDate")
  public Generic<Date> testGenericDate(@RequestBody Generic<Date> input) {
    Assert.isInstanceOf(Generic.class, input);
    Assert.isInstanceOf(Date.class, input.value);
    System.out.println(input.value);
    return input;
  }

  @PostMapping(path = "/genericEnum")
  public Generic<HttpStatus> testGenericEnum(@RequestBody Generic<HttpStatus> input) {
    Assert.isInstanceOf(Generic.class, input);
    Assert.isInstanceOf(HttpStatus.class, input.value);
    return input;
  }

  @PostMapping(path = "/genericGenericUser")
  public Generic<Generic<User>> testGenericGenericUser(@RequestBody Generic<Generic<User>> input) {
    Assert.isInstanceOf(Generic.class, input);
    Assert.isInstanceOf(Generic.class, input.value);
    Assert.isInstanceOf(User.class, input.value.value);
    return input;
  }

  private boolean testvoidInRPCSuccess = false;

  @GetMapping(path = "/testvoidInRPC")
  public void testvoidInRPC() {
    LOGGER.info("testvoidInRPC() is called!");
    testvoidInRPCSuccess = true;
  }

  private boolean testVoidInRPCSuccess = false;

  @GetMapping(path = "/testVoidInRPC")
  public Void testVoidInRPC() {
    LOGGER.info("testVoidInRPC() is called!");
    testVoidInRPCSuccess = true;
    return null;
  }

  private boolean testvoidInRestTemplateSuccess = false;

  @GetMapping(path = "/testvoidInRestTemplate")
  public void testvoidInRestTemplate() {
    LOGGER.info("testvoidInRestTemplate() is called!");
    testvoidInRestTemplateSuccess = true;
  }

  private boolean testVoidInRestTemplateSuccess = false;

  @GetMapping(path = "/testVoidInRestTemplate")
  public Void testVoidInRestTemplate() {
    LOGGER.info("testVoidInRestTemplate() is called!");
    testVoidInRestTemplateSuccess = true;
    return null;
  }

  @GetMapping(path = "/checkVoidResult")
  public boolean checkVoidResult() {
    LOGGER.info("checkVoidResult() is called!");
    return testvoidInRPCSuccess && testVoidInRPCSuccess && testvoidInRestTemplateSuccess
        && testVoidInRestTemplateSuccess;
  }

  /**
   * Simple query object test, users can use it mixed with InvocationContext and plain query param, RequestBody
   */
  @PostMapping(path = "/checkQueryObject")
  public String checkQueryObject(Person person, @RequestParam(name = "otherName") String otherName,
      InvocationContext invocationContext, @RequestBody Person requestBody) {
    LOGGER.info("checkQueryObject() is called!");
    return "invocationContext_is_null=" + (null == invocationContext) + ",person="
        + person + ",otherName=" + otherName + ",name=" + person.getName() + ",requestBody=" + requestBody;
  }

  /**
   * For the nesting object params, including the generic params whose generic field is an object,
   * the inner object field is not supported.
   */
  @PutMapping(path = "/checkQueryGenericObject")
  public String checkQueryGenericObject(@RequestBody GenericParam<Person> requestBody,
      GenericParamWithJsonIgnore<Person> generic) {
    LOGGER.info("checkQueryGenericObject() is called!");
    return "str=" + generic.getStr() + ",generic=" + generic + ",requestBody=" + requestBody;
  }

  /**
   * If the generic field is simple type, it's supported to be deserialized.
   * The same for those simple type field inherited from the parent class.
   */
  @PutMapping(path = "/checkQueryGenericString")
  public String checkQueryGenericString(@RequestBody GenericParam<Person> requestBody,
      GenericParamExtended<String> generic) {
    LOGGER.info("checkQueryGenericObject() is called!");
    return "str=" + generic.getStr() + ",generic=" + generic + ",requestBody=" + requestBody;
  }

  @GetMapping(path = "/testDelay")
  public String testDelay() {
    LOGGER.info("testDelay() is called!");
    return "OK";
  }

  @GetMapping(path = "/testAbort")
  public String testAbort() {
    LOGGER.info("testAbort() is called!");
    return "OK";
  }

  @GetMapping(path = "/testDecodeResponseError")
  public DecodeTestResponse testDecodeResponseError() {
    DecodeTestResponse response = new DecodeTestResponse();
    response.setContent("returnOK");
    return response;
  }

  @RequestMapping(path = "/testDefaultGetApiExample")
  public void testDefaultGetApiExample() {
    LOGGER.info("testDefaultGetApiExample() is called!");
  }

  @RequestMapping(path = "/testHolder")
  public void testHolder(@RequestBody Holder name) {
    LOGGER.info("testDefaultGetApiExample() is called!");
  }
}
