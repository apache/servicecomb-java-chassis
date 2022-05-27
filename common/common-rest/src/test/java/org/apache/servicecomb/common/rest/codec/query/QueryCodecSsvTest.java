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

package org.apache.servicecomb.common.rest.codec.query;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class QueryCodecSsvTest extends QueryCodecTestBase {
  @BeforeEach
  void setUp() {
    codec = new QueryCodecSsv();
  }

  @Nested
  class Encode {
    @Test
    void should_encode_date() throws Exception {
      should_encode("?q=1970-01-01T00%3A00%3A00.000%2B00%3A00", new Date(0));
    }

    @Test
    void should_encode_single_value() throws Exception {
      should_encode("?q=v1", "v1");
    }

    @Test
    void should_encode_empty_string() throws Exception {
      should_encode("?q=", "");
    }

    @Test
    void should_encode_common_string() throws Exception {
      should_encode("?q=v1+v2", "v1", "v2");
    }

    @Test
    void should_encode_common_numbers() throws Exception {
      should_encode("?q=1+2", 1, 2);
    }

    @Test
    void should_encode_chinese_values() throws Exception {
      should_encode("?q=%E4%B8%AD%E6%96%87+v2", "中文", "v2");
    }

    @Test
    void should_encode_ignore_null() throws Exception {
      should_encode("?q=v1+v2", "v1", null, "v2");
    }

    @Test
    void should_encode_when_values_is_empty_after_ignore_null() throws Exception {
      should_encode("", new Object[] {null});
    }
  }

  @Nested
  class Decode {
    @Test
    void should_decode_single_value_to_array() {
      should_decode("1", new int[] {1});
    }

    @Test
    void should_decode_common_values_to_array() {
      should_decode("1 2", new int[] {1, 2});
    }

    @Test
    void should_decode_null_to_array() {
      should_decode((String) null, new int[] {});
    }

    @Test
    void should_decode_empty_string_to_number() {
      should_decode("", new int[] {0});
    }

    @Test
    void should_decode_empty_string_to_string() {
      should_decode("", new String[] {""});
    }

    @Test
    void should_decode_common_values_with_empty_string_to_array() {
      should_decode("1  2", new int[] {1, 0, 2});
    }

    @Test
    void should_decode_values_end_with_delimiter() {
      should_decode("1  ", new int[] {1, 0, 0});
    }

    @Test
    void should_decode_values_start_with_delimiter() {
      should_decode("  1", new int[] {0, 0, 1});
    }
  }
}