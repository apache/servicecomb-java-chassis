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

package org.apache.servicecomb.it.testcase;

import static org.junit.Assert.assertEquals;

import org.apache.servicecomb.it.extend.engine.GateRestTemplate;
import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

public class TestParamCodecEdge {
  private static RestTemplate client;

  private static String producerName;

  @Before
  public void prepare() {
    if (!ITJUnitUtils.getProducerName().equals(producerName)) {
      producerName = ITJUnitUtils.getProducerName();
      client = new GateRestTemplate("it-edge", producerName, "paramCodec");
    }
  }

  @Test
  public void spaceCharEncode() {
    String paramString = "a%2B+%20b%% %20c";
    String result = client.getForObject("/spaceCharCodec/" + paramString + "?q=" + paramString, String.class);
    assertEquals(paramString + " +%20%% " + paramString + " true", result);
  }
}
