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
package org.apache.servicecomb.swagger.invocation.response;

import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

public class TestResponsesMeta {
  static class ResponseMetaImpl {
    @ApiResponses({@ApiResponse(responseCode = "400", description = "",
        content = {@Content(schema = @Schema(type = "string"))}),
        @ApiResponse(responseCode = "401", description = "",
            content = {@Content(schema = @Schema(implementation = String.class))},
            headers = {@Header(name = "h1", schema = @Schema(implementation = String.class))})
    })
    public int add(int x, int y) {
      return x + y;
    }
  }

  @Test
  public void test() {
    OpenAPI swagger = SwaggerGenerator.generate(ResponseMetaImpl.class);
    Operation operation = swagger.getPaths().get("/add").getPost();

    ResponsesMeta meta = new ResponsesMeta();
    meta.init(swagger, operation);

    JavaType resp = meta.findResponseType(200);
    Assertions.assertEquals(Integer.class, resp.getRawClass());

    resp = meta.findResponseType(201);
    Assertions.assertEquals(Integer.class, resp.getRawClass());

    resp = meta.findResponseType(400);
    Assertions.assertEquals(String.class, resp.getRawClass());

    resp = meta.findResponseType(401);
    Assertions.assertEquals(String.class, resp.getRawClass());

    resp = meta.findResponseType(500);
    // changed to Object for new version to keep user defined error data not lose and can be parsed.
    Assertions.assertEquals(Object.class, resp.getRawClass());
  }
}
