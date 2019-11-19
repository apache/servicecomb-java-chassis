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

import org.apache.servicecomb.foundation.test.scaffolding.model.Media;
import org.apache.servicecomb.it.extend.engine.GateRestTemplate;
import org.junit.Test;

public class TestParamCodecEdge {
  static GateRestTemplate client = GateRestTemplate.createEdgeRestTemplate("paramCodec");

  @Test
  public void spaceCharEncode() {
    String paramString = "a%2B+%20b%% %20c";
    String paramQueryStringResult = "a%2B %20b%% %20c";
    String result = client.getForObject("/spaceCharCodec/" + paramString + "?q=" + paramString, String.class);
    assertEquals(matchOr(result, paramString + " +%20%% " + paramQueryStringResult + " true",
        paramString + " +%20%% " + paramString + " true"), result);
  }

  private String matchOr(String result, String expected1, String expected2) {
    // spring mvc & rpc handles "+' differently, both '+' or ' ' is correct according to HTTP SPEC. spring mvc changed from '+' to ' ' since spring 5.
    if (result.equals(expected1)) {
      return expected1;
    }
    return expected2;
  }

  @Test
  public void enumSpecialName() {
    assertEquals(Media.AAC,
        client.postForObject("/enum/enumSpecialName", Media.AAC, Media.class));
    assertEquals(Media.FLAC,
        client.postForObject("/enum/enumSpecialName", Media.FLAC, Media.class));
    assertEquals(Media.H_264,
        client.postForObject("/enum/enumSpecialName", Media.H_264, Media.class));
    assertEquals(Media.MPEG_2,
        client.postForObject("/enum/enumSpecialName", Media.MPEG_2, Media.class));
    assertEquals(Media.WMV,
        client.postForObject("/enum/enumSpecialName", Media.WMV, Media.class));
  }
}
