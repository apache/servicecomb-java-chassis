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

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.vertx.core.json.Json;

@RestSchema(schemaId = "springMVCObjectRestSchemaRestOnly")
@RequestMapping(path = "/v1/springMVCObjectRestSchemaRestOnly")
public class SpringMVCObjectRestSchemaRestOnly {
  private static final Logger LOGGER = LoggerFactory.getLogger(SpringMVCObjectRestSchemaRestOnly.class);

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
  public TestNullFieldAndDefaultValueParamProducer testNullFieldAndDefaultValue(
      @RequestBody TestNullFieldAndDefaultValueParamProducer request) {
    String jsonRequest = Json.encode(request);
    request.setRawRequest(jsonRequest);
    LOGGER.info("return testNullFieldAndDefaultValue response: {}", request);
    LOGGER.info("raw json is {}", Json.encode(request));
    return request;
  }
}
