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

import org.apache.servicecomb.it.extend.engine.GateRestTemplate;
import org.junit.jupiter.api.Test;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TestDownloadSlowStreamEdge {
  static GateRestTemplate client = GateRestTemplate.createEdgeRestTemplate("download");

  @Test
  public void clearInputStreamAfterDisconnect() throws IOException {
    OkHttpClient httpClient = new OkHttpClient();
    Request request = new Request.Builder().url(client.getUrlPrefix() + "/slowInputStream")
        .build();
    Response response = httpClient.newCall(request).execute();

    response.body().byteStream();
    response.body().close();

    client.getForObject("/waitSlowInputStreamClosed", Void.class);
  }
}
