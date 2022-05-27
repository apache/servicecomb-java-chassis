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

package org.apache.servicecomb.core.definition;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.Const;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OperationConfigTest {
  OperationConfig config = new OperationConfig();

  @Nested
  class InPoolTimeout {
    @Test
    void should_get_rest_value() {
      config.setMsDefaultRequestWaitInPoolTimeout(1);
      config.setMsRestRequestWaitInPoolTimeout(2);

      long nano = TimeUnit.MILLISECONDS.toNanos(2);
      assertThat(config.getNanoRestRequestWaitInPoolTimeout()).isEqualTo(nano);
      assertThat(config.getNanoRequestWaitInPoolTimeout(Const.RESTFUL)).isEqualTo(nano);
    }

    @Test
    void should_get_highway_value() {
      config.setMsDefaultRequestWaitInPoolTimeout(1);
      config.setMsHighwayRequestWaitInPoolTimeout(2);

      long nano = TimeUnit.MILLISECONDS.toNanos(2);
      assertThat(config.getNanoHighwayRequestWaitInPoolTimeout()).isEqualTo(nano);
      assertThat(config.getNanoRequestWaitInPoolTimeout(Const.HIGHWAY)).isEqualTo(nano);
    }

    @Test
    void should_support_register_customize_transport() {
      config.setMsDefaultRequestWaitInPoolTimeout(1);
      config.registerRequestWaitInPoolTimeout("abc", 2);

      long nano = TimeUnit.MILLISECONDS.toNanos(2);
      assertThat(config.getNanoRequestWaitInPoolTimeout("abc")).isEqualTo(nano);
    }

    @Test
    void should_get_invocation_timeout_value() {
      config.setMsInvocationTimeout(1);

      long nano = TimeUnit.MILLISECONDS.toNanos(1);
      assertThat(config.getNanoInvocationTimeout()).isEqualTo(nano);
      assertThat(config.getMsInvocationTimeout()).isEqualTo(1);
    }
  }
}