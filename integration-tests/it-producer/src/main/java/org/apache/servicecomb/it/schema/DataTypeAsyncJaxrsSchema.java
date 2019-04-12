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

package org.apache.servicecomb.it.schema;

import java.util.concurrent.CompletableFuture;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.http.ResponseEntity;

@RestSchema(schemaId = "dataTypeAsyncJaxrs")
@Path("/v1/dataTypeAsyncJaxrs")
public class DataTypeAsyncJaxrsSchema {
  @Path("/responseEntityString")
  @GET
  public CompletableFuture<ResponseEntity<String>> responseEntityString() {
    CompletableFuture<ResponseEntity<String>> responseFuture = new CompletableFuture<>();
    ResponseEntity<String> responseEntity =
        ResponseEntity.status(203).header("testH", "testV1", "testV2").body("TestOK");
    responseFuture.complete(responseEntity);
    return responseFuture;
  }

  @Path("/responseEntityDataObject")
  @GET
  public CompletableFuture<ResponseEntity<DefaultJsonValueResponse>> responseEntityDataObject() {
    CompletableFuture<ResponseEntity<DefaultJsonValueResponse>> responseFuture = new CompletableFuture<>();
    DefaultJsonValueResponse body = new DefaultJsonValueResponse();
    body.setMessage("TestOK");
    body.setType(2);
    ResponseEntity<DefaultJsonValueResponse> responseEntity =
        ResponseEntity.status(203).header("testH", "testV1", "testV2").body(body);
    responseFuture.complete(responseEntity);
    return responseFuture;
  }
}
