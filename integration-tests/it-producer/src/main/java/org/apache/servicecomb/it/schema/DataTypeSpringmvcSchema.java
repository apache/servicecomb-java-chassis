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

  @GetMapping(path = "/add")
  public int add(int a, int b) {
    return a + b;
  }
}
