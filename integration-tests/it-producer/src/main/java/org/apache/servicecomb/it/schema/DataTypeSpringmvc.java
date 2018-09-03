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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestSchema(schemaId = "dataTypeSpringmvc")
@RequestMapping(path = "/v1/dataTypeSpringmvc")
public class DataTypeSpringmvc {
  private DataTypePojo pojo = new DataTypePojo();

  @GetMapping(path = "checkTransport")
  public String checkTransport(HttpServletRequest request) {
    return pojo.checkTransport(request);
  }

  @GetMapping("intPath/{input}")
  public int intPath(@PathVariable("input") int input) {
    return pojo.intBody(input);
  }

  @GetMapping("intQuery")
  public int intQuery(@RequestParam("input") int input) {
    return pojo.intBody(input);
  }

  @GetMapping("intHeader")
  public int intHeader(@RequestHeader("input") int input) {
    return pojo.intBody(input);
  }

  @GetMapping("intCookie")
  public int intCookie(@CookieValue("input") int input) {
    return pojo.intBody(input);
  }

  @PostMapping("intBody")
  public int intBody(@RequestBody int input) {
    return pojo.intBody(input);
  }

  @PostMapping(path = "intForm")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "form1", dataType = "integer", format = "int32", paramType = "form", value = "a required form param",
          required = true)})
  public int intForm(int form1) {
    return pojo.intBody(form1);
  }

  @RequestMapping(path = "intAttribute", method = RequestMethod.POST)
  public int intAttribute(@RequestAttribute("a") int a) {
    return pojo.intBody(a);
  }

  @GetMapping("intQueryWithDefault")
  public int intQueryWithDefault(@RequestParam(value = "input", defaultValue = "13") int input) {
    return pojo.intBody(input);
  }

  @GetMapping("intHeaderWithDefault")
  public int intHeaderWithDefault(@RequestHeader(value = "input", defaultValue = "13") int input) {
    return pojo.intBody(input);
  }

  //暂时不支持cookie默认值,不过以后会修复,先放这里,不影响
  @GetMapping("intCookieWithDefault")
  public int intCookieWithDefault(@CookieValue(value = "input", defaultValue = "13") int input) {
    return pojo.intBody(input);
  }

  @PostMapping(path = "intFormWithDefault")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "form1", dataType = "integer", format = "int32", paramType = "form", value = "a required form param", defaultValue = "13",
          required = true)})
  public int intFormWithDefault(int form1) {
    return pojo.intBody(form1);
  }

  //这里算是 jaxrs 和 springmvc 的一个混用,按道理是不能这么使用的,不过这样确实跑通了
  //也算是增加了功能,就不删了
  @RequestMapping(path = "intAttributeWithDefault", method = RequestMethod.POST)
  public int intAttributeWithDefault(@RequestAttribute("a") @DefaultValue("13") int a) {
    return pojo.intBody(a);
  }

  @RequestMapping(path = "add", method = RequestMethod.POST)
  public int intAdd(@RequestAttribute("a") int a, @RequestAttribute("b") int b) {
    return a + b;
  }

  @PostMapping(path = "intMulti/{e}")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "a", dataType = "integer", format = "int32", paramType = "query"),
      @ApiImplicitParam(name = "c", dataType = "integer", format = "int32", paramType = "query"),
      @ApiImplicitParam(name = "d", dataType = "integer", format = "int32", paramType = "header"),
      @ApiImplicitParam(name = "e", dataType = "integer", format = "int32", paramType = "path"),
  })
  public String intMulti(int a, @CookieValue(name = "b") int b, int c, int d, int e) {
    return String.format("a=%s,b=%s,c=%s,d=%s,e=%s", a, b, c, d, e);
  }

  @PostMapping(path = "queryRequest")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "a", dataType = "integer", format = "int32", paramType = "query"),
  })
  public int intRequestQuery(HttpServletRequest request) {
    return Integer.parseInt(request.getParameter("a"));
  }

  @PostMapping(path = "formRequest")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "form1", dataType = "integer", format = "int32", paramType = "form", value = "a required form param",
          required = true),
      @ApiImplicitParam(name = "form2", dataType = "integer", format = "int32", paramType = "form", value = "an optional form param")})
  public String intRequestForm(HttpServletRequest request) {
    int form1 = Integer.parseInt(request.getParameter("form1"));
    int form2 = Integer.parseInt(request.getParameter("form2"));
    return String.format("form1=%s,form2=%s", form1, form2);
  }
}
