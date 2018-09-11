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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.apache.servicecomb.it.extend.engine.GateRestTemplate;
import org.junit.Test;

public class TestRestServerConfigEdge {
  static GateRestTemplate rt = (GateRestTemplate) GateRestTemplate.createEdgeRestTemplate("restServerConfig");

  @Test
  public void testIllegalPathParam() throws IOException {
    String paramString = "%%A";
    String requestUri =
        rt.getUrlPrefix("it-edge", "it-producer", "restServerConfig")
            + "/testIntPathParam/" + paramString;

    URL url = new URL(requestUri);
    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

    String errorBody = null;
    int responseCode = urlConnection.getResponseCode();
    String responseMessage = urlConnection.getResponseMessage();
    try (Scanner scanner = new Scanner(urlConnection.getErrorStream())) {
      errorBody = scanner.nextLine();
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    urlConnection.disconnect();

    assertEquals(500, responseCode);
    assertEquals("Internal Server Error", responseMessage);
    assertEquals("unknown error", errorBody);
  }
}
