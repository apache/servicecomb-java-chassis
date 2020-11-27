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

package org.apache.servicecomb.foundation.common.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.vertx.core.json.Json;
import io.vertx.core.json.jackson.DatabindCodec;

class DynamicEnumTest {
  static final String UNKNOWN_COLOR = "\"UNKNOWN-COLOR\"";

  static class Color extends DynamicEnum<String> {
    public static final Color RED = new Color("RED");

    public static final Color BLUE = new Color("BLUE");

    private static final DynamicEnumCache<Color> CACHE = new DynamicEnumCache<>(Color.class);

    public Color(String value) {
      super(value);
    }

    @JsonCreator
    public static Color fromValue(String value) {
      return CACHE.fromValue(value);
    }
  }

  static class ColorModel {
    public Color color;
  }

  @Test
  void should_encode() {
    assertThat(Json.encode(Color.RED)).isEqualTo("\"RED\"");
  }

  @Test
  void should_be_null_when_convert_from_null() {
    assertThat(DatabindCodec.mapper().convertValue(null, Color.class)).isNull();
  }

  @Test
  void should_be_null_when_decode_from_null() {
    ColorModel model = Json.decodeValue(Json.encode(new ColorModel()), ColorModel.class);
    assertThat(model.color).isNull();
  }

  @Test
  void should_decode_from_known_value() {
    Color color = Json.decodeValue(Json.encode(Color.RED), Color.class);
    assertThat(color).isEqualTo(Color.RED);
  }

  @Test
  void should_decode_from_unknown_value() {
    Color color = Json.decodeValue(UNKNOWN_COLOR, Color.class);
    assertThat(color).isEqualTo(Color.fromValue("UNKNOWN-COLOR"));
  }

  @Test
  void should_not_cache_unknown_value() {
    Color value1 = Json.decodeValue(UNKNOWN_COLOR, Color.class);
    Color value2 = Json.decodeValue(UNKNOWN_COLOR, Color.class);
    assertThat(value1).isNotSameAs(value2);
  }
}