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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;

@RestSchema(schemaId = "apiParamSpringmvc")
@RequestMapping(path = "/apiParamSpringmvc")
public class ApiParamSpringmvcSchema {
  @PostMapping(path = "/body")
  public void body(@ApiParam(value = "desc of body param",
      required = true,
      name = "modelEx",
      examples = @Example(value = {
          @ExampleProperty(mediaType = "k1", value = "v1"),
          @ExampleProperty(mediaType = "k2", value = "v2")})) @RequestBody CommonModel model) {

  }

  @PostMapping(path = "/query")
  public void query(@ApiParam(value = "desc of query param",
      required = true,
      readOnly = true,
      allowEmptyValue = true,
      name = "inputEx",
      example = "10") int input) {

  }

  @PostMapping(path = "/queryArr")
  public void queryArr(@ApiParam(value = "desc of queryArr param",
      required = true,
      readOnly = true,
      allowEmptyValue = true,
      name = "inputEx",
      example = "10",
      collectionFormat = "csv") int[] inputArr) {

  }

  @PostMapping(path = "/header")
  public void header(@ApiParam(value = "desc of header param") @RequestHeader("input") int input) {

  }

  @PostMapping(path = "/cookie")
  public void cookie(@ApiParam(value = "desc of cookie param") @CookieValue("input") int input) {

  }

  @PostMapping(path = "/form")
  public void form(@ApiParam(value = "desc of form param") @RequestAttribute("input") int input) {

  }
}
