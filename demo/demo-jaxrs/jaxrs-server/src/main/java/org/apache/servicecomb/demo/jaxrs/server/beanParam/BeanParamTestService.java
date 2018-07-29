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

package org.apache.servicecomb.demo.jaxrs.server.beanParam;

import java.io.IOException;
import java.util.Scanner;

import javax.servlet.http.Part;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;

@RestSchema(schemaId = "beanParamTest")
@Path("/beanParamTest")
public class BeanParamTestService {
  @Path("/{pathSwaggerStr}/simple")
  @GET
  public String beanParameterTest(InvocationContext invocationContext, @BeanParam TestBeanParameter testBeanParameter,
      @QueryParam("extraQuery") String extraQuery) {
    return String.format("invocationContextConsistency=%b|testBeanParameter=%s|extraQuery=%s",
        ContextUtils.getInvocationContext() == invocationContext,
        testBeanParameter.toString(),
        extraQuery);
  }

  @Path("/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @POST
  public String beanParameterTestUpload(@FormParam("up0") Part up0,
      @BeanParam TestBeanParameterWithUpload testBeanParameter,
      @QueryParam("extraQuery") String extraQuery) throws IOException {
    return String.format("testBeanParameter=%s|extraQuery=%s|up0=%s|up1=%s|up2=%s",
        testBeanParameter.toString(),
        extraQuery,
        getUploadContent(up0),
        getUploadContent(testBeanParameter.getUp1()),
        getUploadContent(testBeanParameter.getUp2()));
  }

  public String getUploadContent(Part upload) throws IOException {
    StringBuilder result = new StringBuilder();
    try (Scanner scanner = new Scanner(upload.getInputStream())) {
      while (scanner.hasNext()) {
        result.append(scanner.next());
      }
    }
    return result.toString();
  }
}
