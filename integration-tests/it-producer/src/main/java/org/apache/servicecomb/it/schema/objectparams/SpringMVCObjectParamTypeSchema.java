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

package org.apache.servicecomb.it.schema.objectparams;

import java.util.List;
import java.util.Map;

import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.vertx.core.json.Json;

@RestSchema(schemaId = "SpringMVCObjectParamTypeSchema")
@RequestMapping(path = "/v1/springMVCObjectParamTypeSchema")
public class SpringMVCObjectParamTypeSchema implements ObjectParamTypeSchema {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringMVCObjectParamTypeSchema.class);

  @PostMapping("testFlattenObjectParam")
  @Override
  public FlattenObjectResponse testFlattenObjectParam(@RequestBody FlattenObjectRequest request) {
    return RestObjectMapperFactory.getRestObjectMapper().convertValue(request, FlattenObjectResponse.class);
  }

  @PutMapping("testMultiLayerObjectParam")
  @Override
  public MultiLayerObjectParam testMultiLayerObjectParam(@RequestBody(required = false) MultiLayerObjectParam request) {
    return request;
  }

  @PostMapping("testRecursiveObjectParam")
  @Override
  public RecursiveObjectParam testRecursiveObjectParam(@RequestBody RecursiveObjectParam request) {
    return request;
  }

  @PostMapping("testListObjectParam")
  @Override
  public List<GenericObjectParam<List<RecursiveObjectParam>>> testListObjectParam(
      @RequestBody List<GenericObjectParam<List<RecursiveObjectParam>>> request) {
    return request;
  }

  @PostMapping("testMapObjectParam")
  @Override
  public Map<String, GenericObjectParam<Map<String, GenericObjectParam<RecursiveObjectParam>>>> testMapObjectParam(
      @RequestBody Map<String, GenericObjectParam<Map<String, GenericObjectParam<RecursiveObjectParam>>>> request) {
    return request;
  }

  /**
   * <a href="https://issues.apache.org/jira/browse/SCB-708">SCB-708</a> SpringMVC only
   */
  @GetMapping("testQueryObjectParam")
  public FlattenObjectRequest testQueryObjectParam(FlattenObjectRequest request) {
    return request;
  }

  /**
   * Request body doesn't carry a certain fields, and will not overwrite the default field value of
   * provider param definition.
   * <p/>
   * There are two test cases:
   * <ul>
   *   <li>consumer invoke provider directly</li>
   *   <li>consumer invoke provider via EdgeService</li>
   * </ul>
   *
   */
  @PostMapping("testNullFieldAndDefaultValue")
  public TestNullFieldAndDefaultValueParam testNullFieldAndDefaultValue(
      @RequestBody TestNullFieldAndDefaultValueParam request) {
    String jsonRequest = Json.encode(request);
    request.setRawRequest(jsonRequest);
    LOGGER.info("return testNullFieldAndDefaultValue response: {}", request);
    LOGGER.info("raw json is {}", Json.encode(request));
    return request;
  }
}
