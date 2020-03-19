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
import static org.junit.Assert.fail;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.foundation.test.scaffolding.model.Media;
import org.apache.servicecomb.it.extend.engine.GateRestTemplate;
import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestParamCodecEdge {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestParamCodecEdge.class);

  static GateRestTemplate client = GateRestTemplate.createEdgeRestTemplate("paramCodec");
  static GateRestTemplate restOnlyClient = GateRestTemplate.createEdgeRestTemplate("paramCodecRestOnly");

  @Test
  public void spaceCharEncode() {
    String paramString = "a%2B+%20b%% %20c";
    String paramQueryStringResult = "a%2B %20b%% %20c";
    String result = client.getForObject("/spaceCharCodec/" + paramString + "?q=" + paramString, String.class);
    assertEquals(matchOr(result, paramString + " +%20%% " + paramQueryStringResult + " true",
        paramString + " +%20%% " + paramString + " true"), result);
    result = client.getForObject("/spaceCharCodec/" + paramString + "?q=" + paramString, String.class);
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
        restOnlyClient.postForObject("/enum/enumSpecialName", Media.AAC, Media.class));
    assertEquals(Media.FLAC,
        restOnlyClient.postForObject("/enum/enumSpecialName", Media.FLAC, Media.class));
    assertEquals(Media.H_264,
        restOnlyClient.postForObject("/enum/enumSpecialName", Media.H_264, Media.class));
    assertEquals(Media.MPEG_2,
        restOnlyClient.postForObject("/enum/enumSpecialName", Media.MPEG_2, Media.class));
    assertEquals(Media.WMV,
        restOnlyClient.postForObject("/enum/enumSpecialName", Media.WMV, Media.class));
  }

  @Test
  public void testStringUrlEncodedForm() throws IOException {
    doTestStringUrlEncodedForm();
    doTestStringUrlEncodedForm();
  }

  private void doTestStringUrlEncodedForm() throws IOException {
    String requestUri = client.getUrlPrefix() + "/stringUrlencodedForm";
    URL url = new URL(requestUri);
    HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
    httpConnection.setDoOutput(true);
    httpConnection.setRequestMethod("POST");
    httpConnection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
    httpConnection.setUseCaches(false);
    httpConnection.connect();
    try (DataOutputStream dataOutputStream = new DataOutputStream(httpConnection.getOutputStream())) {
      dataOutputStream.writeBytes("A=aaa&B=ddd");
      dataOutputStream.flush();
    } catch (IOException e) {
      LOGGER.error("failed to write buffer!", e);
      fail("failed to write buffer!");
      return;
    }

    StringBuilder responseBody = new StringBuilder();
    try (Scanner scanner = new Scanner(httpConnection.getInputStream())) {
      while (scanner.hasNextLine()) {
        responseBody.append(scanner.nextLine());
      }
    }
    Assert.assertEquals("{\"A\":\"aaa\",\"B\":\"ddd\",\"param0\":\"" + ITJUnitUtils.getProducerName() + "\","
            + "\"param1\":\"v1\",\"param2\":\"paramCodec/stringUrlencodedForm\"}",
        responseBody.toString());
    Assert.assertEquals(200, httpConnection.getResponseCode());
  }
}
