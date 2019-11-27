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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.servicecomb.foundation.test.scaffolding.model.Media;
import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.junit.Test;

public class TestParamCodec {
  interface ParamCodecSchemaIntf {
    String spaceCharCodec(String pathVal, String q);

    Media enumSpecialName(Media media);

    Map<String, String> getInvocationContext();
  }

  static Consumers<ParamCodecSchemaIntf> consumers = new Consumers<>("paramCodec", ParamCodecSchemaIntf.class);

  @Test
  public void spaceCharEncode_intf() {
    String paramString = "a%2B+%20b%% %20c";
    String paramQueryStringResult = "a%2B %20b%% %20c";
    String result = consumers.getIntf().spaceCharCodec(paramString, paramString);
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
  public void spaceCharEncode_rt() {
    String paramString = "a%2B+%20b%% %20c";
    String paramQueryStringResult = "a%2B %20b%% %20c";
    String result = consumers.getSCBRestTemplate()
        .getForObject("/spaceCharCodec/" + paramString + "?q=" + paramString, String.class);
    assertEquals(matchOr(result, paramString + " +%20%% " + paramQueryStringResult + " true",
        paramString + " +%20%% " + paramString + " true"), result);
  }

  @Test
  public void enumSpecialName_intf() {
    assertEquals(Media.AAC, consumers.getIntf().enumSpecialName(Media.AAC));
    assertEquals(Media.FLAC, consumers.getIntf().enumSpecialName(Media.FLAC));
    assertEquals(Media.H_264, consumers.getIntf().enumSpecialName(Media.H_264));
    assertEquals(Media.MPEG_2, consumers.getIntf().enumSpecialName(Media.MPEG_2));
    assertEquals(Media.WMV, consumers.getIntf().enumSpecialName(Media.WMV));
  }

  @Test
  public void enumSpecialName_rt() {
    assertEquals(Media.AAC,
        consumers.getSCBRestTemplate().postForObject("/enum/enumSpecialName", Media.AAC, Media.class));
    assertEquals(Media.FLAC,
        consumers.getSCBRestTemplate().postForObject("/enum/enumSpecialName", Media.FLAC, Media.class));
    assertEquals(Media.H_264,
        consumers.getSCBRestTemplate().postForObject("/enum/enumSpecialName", Media.H_264, Media.class));
    assertEquals(Media.MPEG_2,
        consumers.getSCBRestTemplate().postForObject("/enum/enumSpecialName", Media.MPEG_2, Media.class));
    assertEquals(Media.WMV,
        consumers.getSCBRestTemplate().postForObject("/enum/enumSpecialName", Media.WMV, Media.class));
  }

  @Test
  public void testInvocationContext() {
    ContextUtils.setInvocationContext(new InvocationContext());
    ContextUtils.getInvocationContext().addContext("testKey", "testValue");
    Map<String, String> resultContext = consumers.getIntf().getInvocationContext();
    assertEquals(resultContext.toString(), 3, resultContext.size());
    assertEquals("testValue", resultContext.get("testKey"));
    assertEquals("it-consumer", resultContext.get("x-cse-src-microservice"));
    assertNotNull(resultContext.get("X-B3-TraceId"));

    ContextUtils.getInvocationContext().addContext("testKey2", "testValue2");
    ContextUtils.getInvocationContext().addContext("X-B3-TraceId", "5cba93e1d0a9cdee");
    resultContext = consumers.getIntf().getInvocationContext();
    assertEquals(4, resultContext.size());
    assertEquals("testValue", resultContext.get("testKey"));
    assertEquals("testValue2", resultContext.get("testKey2"));
    assertEquals("it-consumer", resultContext.get("x-cse-src-microservice"));
    assertEquals("5cba93e1d0a9cdee", resultContext.get("X-B3-TraceId"));
  }
}
