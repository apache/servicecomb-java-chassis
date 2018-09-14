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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

@RestSchema(schemaId = "annotatedAttributeSpringmvc")
@RequestMapping(path = "/v1/annotatedAttributeSpringmvc")
public class AnnotatedAttributeSpringmvcSchema {

  @GetMapping("fromHeader")
  public String fromHeader(@RequestHeader("input") String inputs, @RequestHeader(value = "input2") String inputs2,
      @RequestHeader(name = "input3") String inputs3) {
    return inputs + "," + inputs2 + "," + inputs3;
  }

  @GetMapping("fromQuery")
  public String fromQuery(@RequestParam("input") String inputs, @RequestParam(value = "input2") String inputs2,
      @RequestParam(name = "input3") String inputs3) {
    return inputs + "," + inputs2 + "," + inputs3;
  }

  @GetMapping("fromCookie")
  public String fromCookie(@CookieValue("input") String inputs, @CookieValue(value = "input2") String inputs2,
      @CookieValue(name = "input3") String inputs3) {
    return inputs + "," + inputs2 + "," + inputs3;
  }

  @GetMapping("fromPath/{input}/{input2}/{input3}")
  public String fromPath(@PathVariable("input") String inputs, @PathVariable(value = "input2") String inputs2,
      @PathVariable(name = "input3") String inputs3) {
    return inputs + "," + inputs2 + "," + inputs3;
  }

  @PostMapping("fromPart")
  public String fromPart(@RequestPart("input") String inputs, @RequestPart(value = "input2") String inputs2,
      @RequestPart(name = "input3") String inputs3) {
    return inputs + "," + inputs2 + "," + inputs3;
  }

  @PostMapping("fromAttribute")
  public String fromAttribute(@RequestAttribute("input") String inputs,
      @RequestAttribute(value = "input2") String inputs2, @RequestAttribute(name = "input3") String inputs3) {
    return inputs + "," + inputs2 + "," + inputs3;
  }
}
