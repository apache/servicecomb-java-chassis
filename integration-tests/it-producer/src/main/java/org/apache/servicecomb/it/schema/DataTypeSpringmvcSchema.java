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

import java.util.Arrays;

import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;

@RestSchema(schemaId = "dataTypeSpringmvc")
@RequestMapping(path = "/v1/dataTypeSpringmvc")
public class DataTypeSpringmvcSchema {
  @GetMapping("intPath/{input}")
  public int intPath(@PathVariable("input") int input) {
    return input;
  }

  @GetMapping("intQuery")
  public int intQuery(@RequestParam("input") int input) {
    return input;
  }

  @GetMapping("intHeader")
  public int intHeader(@RequestHeader("input") int input) {
    return input;
  }

  @GetMapping("intCookie")
  public int intCookie(@CookieValue("input") int input) {
    return input;
  }

  @PostMapping("intForm")
  public int intForm(@RequestAttribute("input") int input) {
    return input;
  }

  @PostMapping("intBody")
  public int intBody(@RequestBody int input) {
    return input;
  }

  @GetMapping(path = "intAdd")
  public int intAdd(int num1, int num2) {
    return num1 + num2;
  }

  //String

  @GetMapping("stringPath/{input}")
  public String stringPath(@PathVariable("input") String input) {
    return input;
  }

  @GetMapping("stringQuery")
  public String stringQuery(@RequestParam("input") String input) {
    return input;
  }

  @GetMapping("stringHeader")
  public String stringHeader(@RequestHeader("input") String input) {
    return input;
  }

  @GetMapping("stringCookie")
  public String stringCookie(@CookieValue("input") String input) {
    return input;
  }

  @PostMapping("stringBody")
  public String stringBody(@RequestBody String input) {
    return input;
  }

  @PostMapping("stringForm")
  public String stringForm(@RequestAttribute("input") String input) {
    return input;
  }

  @GetMapping(path = "stringConcat")
  public String stringConcat(String str1, String str2) {
    return str1 + str2;
  }

  //double
  @GetMapping("doublePath/{input}")
  public double doublePath(@PathVariable("input") double input) {
    return input;
  }

  @GetMapping("doubleQuery")
  public double doubleQuery(@RequestParam("input") double input) {
    return input;
  }

  @GetMapping("doubleHeader")
  public double doubleHeader(@RequestHeader("input") double input) {
    return input;
  }

  @GetMapping("doubleCookie")
  public double doubleCookie(@CookieValue("input") double input) {
    return input;
  }

  @PostMapping("doubleForm")
  public double doubleForm(@RequestAttribute("input") double input) {
    return input;
  }

  @PostMapping("doubleBody")
  public double doubleBody(@RequestBody double input) {
    return input;
  }

  @GetMapping(path = "doubleAdd")
  public double doubleAdd(double num1, double num2) {
    return num1 + num2;
  }

  //float
  @GetMapping("floatPath/{input}")
  public float floatPath(@PathVariable("input") float input) {
    return input;
  }

  @GetMapping("floatQuery")
  public float floatQuery(@RequestParam("input") float input) {
    return input;
  }

  @GetMapping("floatHeader")
  public float floatHeader(@RequestHeader("input") float input) {
    return input;
  }

  @GetMapping("floatCookie")
  public float floatCookie(@CookieValue("input") float input) {
    return input;
  }

  @PostMapping("floatForm")
  public float floatForm(@RequestAttribute("input") float input) {
    return input;
  }

  @PostMapping("floatBody")
  public float floatBody(@RequestBody float input) {
    return input;
  }

  @GetMapping(path = "floatAdd")
  public float floatAdd(float num1, float num2) {
    return num1 + num2;
  }

  @PostMapping(path = "enumBody")
  public Color enumBody(@RequestBody Color color) {
    return color;
  }

  // query array
  @GetMapping("queryArr")
  public String queryArr(@RequestParam("queryArr") String[] queryArr) {
    return Arrays.toString(queryArr) + queryArr.length;
  }

  @GetMapping("queryArrCSV")
  public String queryArrCSV(@ApiParam(collectionFormat = "csv") @RequestParam("queryArr") String[] queryArr) {
    return Arrays.toString(queryArr) + queryArr.length;
  }

  @GetMapping("queryArrSSV")
  public String queryArrSSV(@ApiParam(collectionFormat = "ssv") @RequestParam("queryArr") String[] queryArr) {
    return Arrays.toString(queryArr) + queryArr.length;
  }

  @GetMapping("queryArrTSV")
  public String queryArrTSV(@ApiParam(collectionFormat = "tsv") @RequestParam("queryArr") String[] queryArr) {
    return Arrays.toString(queryArr) + queryArr.length;
  }

  @GetMapping("queryArrPIPES")
  public String queryArrPIPES(@ApiParam(collectionFormat = "pipes") @RequestParam("queryArr") String[] queryArr) {
    return Arrays.toString(queryArr) + queryArr.length;
  }

  @GetMapping("queryArrMULTI")
  public String queryArrMULTI(@ApiParam(collectionFormat = "multi") @RequestParam("queryArr") String[] queryArr) {
    return Arrays.toString(queryArr) + queryArr.length;
  }
}
