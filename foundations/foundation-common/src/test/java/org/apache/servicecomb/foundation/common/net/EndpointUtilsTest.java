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

package org.apache.servicecomb.foundation.common.net;

import static org.apache.servicecomb.foundation.common.net.EndpointUtils.formatFromUri;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EndpointUtilsTest {
  @Nested
  class Http {
    @Test
    void should_convert_without_port() {
      assertThat(formatFromUri("http://host")).isEqualTo("rest://host:80");
    }

    @Test
    void should_convert_with_port() {
      assertThat(formatFromUri("http://host:8080")).isEqualTo("rest://host:8080");
    }

    @Test
    void should_not_lost_query_parameters() {
      assertThat(formatFromUri("http://host?q1=v1&q2=v2")).isEqualTo("rest://host:80?q1=v1&q2=v2");
    }
  }

  @Nested
  class Https {
    @Test
    void should_convert_without_port() {
      assertThat(formatFromUri("https://host")).isEqualTo("rest://host:443?sslEnabled=true");
    }

    @Test
    void should_convert_with_port() {
      assertThat(formatFromUri("https://host:8443")).isEqualTo("rest://host:8443?sslEnabled=true");
    }

    @Test
    void should_not_lost_query_parameters() {
      assertThat(formatFromUri("https://host?q1=v1&q2=v2")).isEqualTo("rest://host:443?q1=v1&q2=v2&sslEnabled=true");
    }
  }

  @Nested
  class H2C {
    @Test
    void should_convert_without_port() {
      assertThat(formatFromUri("h2c://host")).isEqualTo("rest://host:80?protocol=http2");
    }

    @Test
    void should_convert_with_port() {
      assertThat(formatFromUri("h2c://host:8080")).isEqualTo("rest://host:8080?protocol=http2");
    }

    @Test
    void should_not_lost_query_parameters() {
      assertThat(formatFromUri("h2c://host?q1=v1&q2=v2")).isEqualTo("rest://host:80?q1=v1&q2=v2&protocol=http2");
    }
  }

  @Nested
  class H2 {
    @Test
    void should_convert_without_port() {
      assertThat(formatFromUri("h2://host")).isEqualTo("rest://host:443?sslEnabled=true&protocol=http2");
    }

    @Test
    void should_convert_with_port() {
      assertThat(formatFromUri("h2://host:8443")).isEqualTo("rest://host:8443?sslEnabled=true&protocol=http2");
    }

    @Test
    void should_not_lost_query_parameters() {
      assertThat(formatFromUri("h2://host?q1=v1&q2=v2"))
          .isEqualTo("rest://host:443?q1=v1&q2=v2&sslEnabled=true&protocol=http2");
    }
  }

  @Nested
  class NotProvideScheme {
    @Test
    void should_set_scheme_to_h2c() {
      assertThat(formatFromUri("host")).isEqualTo("rest://host:80?protocol=http2");
    }

    @Test
    void should_not_lost_query_parameters() {
      assertThat(formatFromUri("host?q1=v1&q2=v2")).isEqualTo("rest://host:80?q1=v1&q2=v2&protocol=http2");
    }
  }

  @Nested
  class UnknownScheme {
    @Test
    void should_not_change() {
      assertThat(formatFromUri("abc://host:123?q1=v1")).isEqualTo("abc://host:123?q1=v1");
    }
  }
}