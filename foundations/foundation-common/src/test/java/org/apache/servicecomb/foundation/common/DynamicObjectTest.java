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

package org.apache.servicecomb.foundation.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.vertx.core.json.Json;
import io.vertx.core.json.jackson.DatabindCodec;

class DynamicObjectTest {
  @Test
  void should_support_json_encode_decode() {
    Map<String, Object> map = new HashMap<>();
    map.put("k", "v");

    DynamicObject dynamicObject = DatabindCodec.mapper().convertValue(map, DynamicObject.class);

    assertThat(dynamicObject.getDynamic()).isNotSameAs(map);
    assertThat(dynamicObject.getDynamic()).isEqualTo(map);
    assertThat(Json.encode(dynamicObject)).isEqualTo("{\"k\":\"v\"}");
  }
}