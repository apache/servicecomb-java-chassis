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
package org.apache.servicecomb.demo.springmvc.client;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.solution.basic.integration.ManagementEndpoint;
import org.springframework.stereotype.Component;

@Component
public class TestManagementEndpoint implements CategorizedTestCase {
  private static final String CONTENT_SCHEMA = """
      openapi: 3.0.1
      info:
        title: swagger definition for org.apache.servicecomb.demo.springmvc.server.SchemeInterfaceSpringmvc
        version: 1.0.0
      servers:
      - url: /springmvc/schemaInterface
      paths:
        /add:
          get:
            operationId: add
            parameters:
            - name: a
              in: query
              required: true
              schema:
                minimum: 1
                type: integer
                format: int32
            - name: b
              in: query
              required: true
              schema:
                minimum: 1
                type: integer
                format: int32
            responses:
              "200":
                description: response of 200
                content:
                  application/json:
                    schema:
                      type: integer
                      format: int32
        /tailingSlash/:
          get:
            operationId: tailingSlash
            parameters:
            - name: a
              in: query
              required: true
              schema:
                minimum: 1
                type: integer
                format: int32
            - name: b
              in: query
              required: true
              schema:
                minimum: 1
                type: integer
                format: int32
            responses:
              "200":
                description: response of 200
                content:
                  application/json:
                    schema:
                      type: string
      components: {}
      """;

  @RpcReference(microserviceName = "springmvc", schemaId = "SchemeInterfaceSpringmvc")
  private SchemeInterfaceSpringmvc schemeInterfaceSpringmvc;

  @RpcReference(microserviceName = "springmvc", schemaId = ManagementEndpoint.NAME)
  private ManagementEndpoint managementEndpoint;

  @Override
  public void testAllTransport() throws Exception {
    testSchemeInterfaceSpringmvcContentCorrect();
  }

  @Override
  public void testRestTransport() throws Exception {
    testSchemeInterfaceSpringmvcPathSlashCorrect();
  }

  private void testSchemeInterfaceSpringmvcPathSlashCorrect() {
    String result = schemeInterfaceSpringmvc.tailingSlash(3, 5);
    TestMgr.check("/api/springmvc/schemaInterface/tailingSlash/;" +
        "/api/springmvc/schemaInterface/tailingSlash/;" +
        "/api/springmvc/schemaInterface/tailingSlash/;" +
        "-2", result);
  }

  private void testSchemeInterfaceSpringmvcContentCorrect() {
    Set<String> schemaIds = new HashSet<>();
    schemaIds.add("SchemeInterfaceSpringmvc");
    Map<String, String> contents = managementEndpoint.schemaContents(schemaIds);
    TestMgr.check(1, contents.size());
    TestMgr.check(CONTENT_SCHEMA, contents.get("SchemeInterfaceSpringmvc"));
  }
}
