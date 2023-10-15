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
package org.apache.servicecomb.swagger.generator.springmvc.model;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.ws.rs.core.MediaType;

@RestController
public class DefaultParameterSchema {
  @GetMapping("/testSimpleParam")
  public String testSimpleParam(String strParam) {
    return strParam;
  }

  @GetMapping("/testObjectParam")
  public String testObjectParam(TestParam objParam) {
    return objParam.toString();
  }

  @GetMapping("/testUnsupportedParamType")
  public String testUnsupportedParamType(int i, List<TestParam> integerList, Map<String, String> stringMap) {
    return null;
  }

  @RequestMapping(path = "testSingleMediaType", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public String testSingleMediaType(String input) {
    return input;
  }

  @RequestMapping(path = "testMultipleMediaType", method = RequestMethod.PUT, consumes = {MediaType.TEXT_PLAIN,
      MediaType.APPLICATION_JSON},
      produces = {MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
  public String testMultipleMediaType(String input) {
    return input;
  }

  @RequestMapping(path = "testBlankMediaType", method = RequestMethod.POST)
  public String testBlankMediaType(String input) {
    return input;
  }
}
