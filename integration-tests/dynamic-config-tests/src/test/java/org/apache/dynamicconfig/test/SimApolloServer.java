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

package org.apache.dynamicconfig.test;

import io.vertx.core.AbstractVerticle;

public class SimApolloServer extends AbstractVerticle {
  public void start() {
    String response = "{\n"
        + "  \"appId\": \"test\",\n"
        + "  \"clusterName\": \"default\",\n"
        + "  \"namespaceName\": \"application\",\n"
        + "  \"name\": \"20180703151728-release\",\n"
        + "  \"configurations\": {\n"
        + "    \"timeout\": \"6666\"\n"
        + "  },\n"
        + "  \"comment\": \"\",\n"
        + "  \"dataChangeCreatedBy\": \"apollo\",\n"
        + "  \"dataChangeLastModifiedBy\": \"apollo\",\n"
        + "  \"dataChangeCreatedTime\": \"2018-07-03T15:17:32.000+0800\",\n"
        + "  \"dataChangeLastModifiedTime\": \"2018-07-03T15:17:32.000+0800\"\n"
        + "}";

    vertx.createHttpServer().requestHandler(req -> {
      req.response()
          .putHeader("content-type", "application/json")
          .end(response);
    }).listen(23334);
  }
}