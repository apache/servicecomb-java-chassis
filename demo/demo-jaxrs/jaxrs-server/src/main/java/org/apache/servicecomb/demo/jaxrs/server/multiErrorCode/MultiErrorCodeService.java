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

package org.apache.servicecomb.demo.jaxrs.server.multiErrorCode;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.demo.multiErrorCode.MultiRequest;
import org.apache.servicecomb.demo.multiErrorCode.MultiResponse200;
import org.apache.servicecomb.demo.multiErrorCode.MultiResponse400;
import org.apache.servicecomb.demo.multiErrorCode.MultiResponse500;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.extend.annotations.ResponseHeaders;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

@RestSchema(schemaId = "MultiErrorCodeService")
@Path("MultiErrorCodeService")
public class MultiErrorCodeService {
  @Path("/errorCode")
  @POST
  @ApiResponses({
      @ApiResponse(code = 200, response = MultiResponse200.class, message = ""),
      @ApiResponse(code = 400, response = MultiResponse400.class, message = ""),
      @ApiResponse(code = 500, response = MultiResponse500.class, message = "")})
  public MultiResponse200 errorCode(MultiRequest request) {
    if (request.getCode() == 400) {
      MultiResponse400 r = new MultiResponse400();
      r.setCode(400);
      r.setMessage("bad request");
      throw new InvocationException(javax.ws.rs.core.Response.Status.BAD_REQUEST, r);
    } else if (request.getCode() == 500) {
      MultiResponse500 r = new MultiResponse500();
      r.setCode(500);
      r.setMessage("internal error");
      throw new InvocationException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, r);
    } else {
      MultiResponse200 r = new MultiResponse200();
      r.setCode(200);
      r.setMessage("success result");
      return r;
    }
  }

  @Path("/errorCodeWithHeader")
  @POST
  @ApiResponses({
      @ApiResponse(code = 200, response = MultiResponse200.class, message = ""),
      @ApiResponse(code = 400, response = MultiResponse400.class, message = ""),
      @ApiResponse(code = 500, response = MultiResponse500.class, message = "")})
  @ResponseHeaders({@ResponseHeader(name = "x-code", response = String.class)})
  public Response errorCodeWithHeader(MultiRequest request) {
    Response response = new Response();
    if (request.getCode() == 400) {
      MultiResponse400 r = new MultiResponse400();
      r.setCode(400);
      r.setMessage("bad request");
      response.setStatus(Status.BAD_REQUEST);
      response.setResult(r);
      response.getHeaders().addHeader("x-code", "400");
    } else if (request.getCode() == 500) {
      MultiResponse500 r = new MultiResponse500();
      r.setCode(500);
      r.setMessage("internal error");
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
      response.setResult(r);
      response.getHeaders().addHeader("x-code", "500");
    } else {
      MultiResponse200 r = new MultiResponse200();
      r.setCode(200);
      r.setMessage("success result");
      response.setStatus(Status.OK);
      response.setResult(r);
      response.getHeaders().addHeader("x-code", "200");
    }
    return response;
  }
}
