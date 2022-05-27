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

package org.apache.servicecomb.swagger.invocation.exception;

import static com.google.common.collect.ImmutableMap.of;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.vertx.core.json.Json;

class CommonExceptionDataTest {
  @Test
  void should_not_include_code_in_json_when_code_is_null() {
    CommonExceptionData data = new CommonExceptionData("msg");

    assertThat(Json.encode(data)).isEqualTo("{\"message\":\"msg\"}");
  }

  @Test
  void should_include_code_in_json_when_code_is_not_null() {
    CommonExceptionData data = new CommonExceptionData("code", "msg");

    assertThat(Json.encode(data)).isEqualTo("{\"code\":\"code\",\"message\":\"msg\"}");
  }

  @Test
  void should_include_dynamic_field_in_json() {
    CommonExceptionData data = new CommonExceptionData("msg");
    data.putDynamic("k", "v");

    assertThat(Json.encode(data)).isEqualTo("{\"message\":\"msg\",\"k\":\"v\"}");
  }

  @Test
  void should_decode_dynamic_field_from_json() {
    String json = "{\"message\":\"msg\",\"k\":\"v\"}";
    CommonExceptionData data = Json.decodeValue(json, CommonExceptionData.class);

    assertThat(data.getMessage()).isEqualTo("msg");
    assertThat(data.getDynamic()).isEqualTo(of("k", "v"));
    assertThat(Json.encode(data)).isEqualTo(json);
  }
}