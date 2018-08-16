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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestSchema(schemaId = "SpringMvcDefaultValues")
@RequestMapping(path = "/springmvc/SpringMvcDefaultValues", produces = MediaType.APPLICATION_JSON)
public class SpringMvcDefaultValues {

  @PostMapping(path = "/form", consumes = MediaType.APPLICATION_FORM_URLENCODED)
  public String form(@RequestParam(name = "a", defaultValue = "20") int a,
      @RequestParam(name = "b", defaultValue = "bobo") String b) {
    return "Hello " + a + b;
  }

  @PostMapping(path = "/header")
  public String header(@RequestHeader(name = "a", defaultValue = "20") int a,
      @RequestHeader(name = "b", defaultValue = "bobo") String b,
      @RequestHeader(name = "c", defaultValue = "30") Integer c) {
    return "Hello " + a + b + c;
  }

  @GetMapping("/query")
  public String query(@RequestParam(name = "a", defaultValue = "20") int a,
      @RequestParam(name = "b", defaultValue = "bobo") String b,
      @RequestParam(name = "c", defaultValue = "40") Integer c, @RequestParam(name = "d") int d) {
    return "Hello " + a + b + c + d;
  }

  @GetMapping("/query2")
  public String query2(@RequestParam(name = "e", required = false) int e,
      @RequestParam(name = "a", defaultValue = "20") int a,
      @RequestParam(name = "b", defaultValue = "bobo") String b,
      @RequestParam(name = "c", defaultValue = "40") Integer c,
      @Min(value = 20) @Max(value = 30) @RequestParam(name = "d", required = false) int d) {
    return "Hello " + a + b + c + d + e;
  }

  @GetMapping("/query3")
  public String query3(@RequestParam("a") @Min(value = 20) int a, @RequestParam("b") String b) {
    return "Hello " + a + b;
  }

  @PostMapping("/javaprimitiveint")
  public String springJavaPrimitiveInt(@RequestParam(name = "a", required = false) int a,
      @RequestParam(name = "b", defaultValue = "bobo") String b) {
    return "Hello " + a + b;
  }

  @PostMapping("/javaprimitivenumber")
  public String springJavaPrimitiveNumber(@RequestParam(name = "a", required = false) float a,
      @RequestParam(name = "b", required = false) boolean b) {
    return "Hello " + a + b;
  }

  @PostMapping("/javaprimitivestr")
  public String springJavaPrimitiveStr(@RequestParam(name = "a", required = false) int a,
      @RequestParam(name = "b", required = false) String b) {
    if (b == null || b.equals("")) {
      return "Hello";
    }
    return "Hello " + b + a;
  }

  @PostMapping("/javaprimitivecomb")
  public String springJavaPrimitiveCombination(@RequestParam(name = "a", required = false) Integer a,
      @RequestParam(name = "b", required = false) Float b) {
    return "Hello " + a + b;
  }

  @PostMapping("/allprimitivetypes")
  public String allprimitivetypes(@RequestParam(name = "pBoolean", required = false) boolean pBoolean,
      @RequestParam(name = "pChar", required = false) char pChar,
      @RequestParam(name = "pByte", required = false) byte pByte,
      @RequestParam(name = "pShort", required = false) short pShort,
      @RequestParam(name = "pInt", required = false) int pInt,
      @RequestParam(name = "pLong", required = false) long pLong,
      @RequestParam(name = "pFloat", required = false) float pFloat,
      @RequestParam(name = "pDouble", required = false) double pDouble,
      @RequestParam(name = "pDoubleWrap", required = false) Double pDoubleWrap) {
    return "Hello " + pBoolean + ","
        + pChar + ","
        + pByte + ","
        + pShort + ","
        + pInt + ","
        + pLong + ","
        + pFloat + ","
        + pDouble + ","
        + pDoubleWrap;
  }
}
