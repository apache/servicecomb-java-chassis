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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class QueryCodecsTest {
  static class MyQueryCodecCsv extends QueryCodecCsv {
    @Override
    public int getOrder() {
      return 0;
    }
  }

  @Test
  void can_override_by_customize_implement() {
    QueryCodecs queryCodecs = QueryCodecs.createForTest();
    assertThat(queryCodecs.find(QueryCodecCsv.CODEC_NAME).getClass())
        .isEqualTo(QueryCodecCsv.class);

    queryCodecs = new QueryCodecs(singletonList(new MyQueryCodecCsv()));
    assertThat(queryCodecs.find(QueryCodecCsv.CODEC_NAME).getClass())
        .isEqualTo(MyQueryCodecCsv.class);
  }
}