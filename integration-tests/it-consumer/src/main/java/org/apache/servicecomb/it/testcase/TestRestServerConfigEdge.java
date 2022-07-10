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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.apache.servicecomb.it.extend.engine.GateRestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

public class TestRestServerConfigEdge {
  static GateRestTemplate client = GateRestTemplate.createEdgeRestTemplate("dataTypeJaxrs");

  @Test
  public void testIllegalPathParam() throws IOException {
    doTestIllegalPathParam();
    doTestIllegalPathParam();
  }

  private void doTestIllegalPathParam() throws IOException {
    String paramString = "%%A";
    String requestUri = client.getUrlPrefix() + "/intPath/" + paramString;

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

    Assertions.assertEquals(400, responseCode);
    Assertions.assertEquals("Bad Request", responseMessage);
    Assertions.assertEquals("Bad Request", errorBody);
  }

  @Test
  public void test404ThrownByServicCombNotConvertedTo500() {
    doTest404ThrownByServicCombNotConvertedTo500();
    doTest404ThrownByServicCombNotConvertedTo500();
  }

  private void doTest404ThrownByServicCombNotConvertedTo500() {
    String notFoundRequestUri = client.getUrlPrefix() + "/intPath2/123";

    try {
      client.getForEntity(notFoundRequestUri, int.class);
      Assertions.fail("an exception is expected!");
    } catch (RestClientException e) {
      Assertions.assertEquals(404, ((HttpClientErrorException) e).getRawStatusCode());
      Assertions.assertEquals("Not Found", ((HttpClientErrorException) e).getStatusText());
      Assertions.assertEquals("{\"message\":\"Not Found\"}",
          ((HttpClientErrorException) e).getResponseBodyAsString());
    }
  }

  @Test
  public void testFailureHandlerInDispatcher() throws IOException {
    doTestFailureHandlerInDispatcher();
    doTestFailureHandlerInDispatcher();
  }

  private void doTestFailureHandlerInDispatcher() throws IOException {
    String requestUri = client.getUrlPrefix();
    requestUri = requestUri.substring(0, requestUri.indexOf("rest")) + "dispatcherWithFailureHandler/abc";
    URL url = new URL(requestUri);
    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

    StringBuilder errorBody = new StringBuilder();
    int statusCode = urlConnection.getResponseCode();
    String statusMessage = urlConnection.getResponseMessage();
    try (Scanner scanner = new Scanner(urlConnection.getErrorStream())) {
      while (scanner.hasNextLine()) {
        errorBody.append(scanner.nextLine());
      }
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    urlConnection.disconnect();

    Assertions.assertEquals(460, statusCode);
    Assertions.assertEquals("TestFailureHandlerInDispatcher", statusMessage);
    Assertions.assertEquals("text/plain", urlConnection.getHeaderField("Content-Type"));
    Assertions.assertEquals("test-header-value0", urlConnection.getHeaderField("test-header"));
    Assertions.assertEquals("TestFailureHandlerInDispatcher as expected", errorBody.toString());
  }

  @Test
  public void testFailureHandlerInSomewhereElse() throws IOException {
    doTestFailureHandlerInSomewhereElse();
    doTestFailureHandlerInSomewhereElse();
  }

  private void doTestFailureHandlerInSomewhereElse() throws IOException {
    String requestUri = client.getUrlPrefix();
    requestUri = requestUri.substring(0, requestUri.indexOf("rest")) + "dispatcherWithoutFailureHandler/abc";
    URL url = new URL(requestUri);
    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

    StringBuilder errorBody = new StringBuilder();
    int statusCode = urlConnection.getResponseCode();
    String statusMessage = urlConnection.getResponseMessage();
    try (Scanner scanner = new Scanner(urlConnection.getErrorStream())) {
      while (scanner.hasNextLine()) {
        errorBody.append(scanner.nextLine());
      }
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    urlConnection.disconnect();

    Assertions.assertEquals(461, statusCode);
    Assertions.assertEquals("TestFailureHandlerInSomewhereElse", statusMessage);
    Assertions.assertEquals("\"TestFailureHandlerInSomewhereElse as expected\"", errorBody.toString());
  }
}
