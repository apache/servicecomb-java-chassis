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

package org.apache.servicecomb.demo.jaxrs.client.validation;

import java.util.ArrayList;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.jaxrs.server.validation.ValidationModel;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.web.client.RestTemplate;

public class ValidationServiceClient {
  private static RestTemplate template = RestTemplateBuilder.create();

  private static String urlPrefix = "cse://jaxrs/ValidationService";

  public static void run() {
    // highway do not support this feature
    CseContext.getInstance().getConsumerProviderManager().setTransport("jaxrs", "rest");
    testValidation();
  }

  private static void testValidation() {
    ValidationModel model = new ValidationModel();
    model.setAge(20);
    model.setMembers(new ArrayList<>());
    model.setName("name");
    ValidationModel result = template.postForObject(urlPrefix + "/validate", model, ValidationModel.class);
    TestMgr.check(result.getAge(), 20);
    TestMgr.check(result.getName(), "name");
    TestMgr.check(result.getMembers().size(), 0);

    try {
      model.setAge(null);
      template.postForObject(urlPrefix + "/validate", model, ValidationModel.class);
      TestMgr.check(false, true);
    } catch (InvocationException e) {
      TestMgr.check(400, e.getStatus().getStatusCode());
      TestMgr.check(Status.BAD_REQUEST, e.getReasonPhrase());
      TestMgr.check(e.getErrorData().toString().contains("propertyPath=errorCode.request.age"), true);
    }

    try {
      model.setAge(20);
      model.setMembers(null);
      template.postForObject(urlPrefix + "/validate", model, ValidationModel.class);
      TestMgr.check(false, true);
    } catch (InvocationException e) {
      TestMgr.check(400, e.getStatus().getStatusCode());
      TestMgr.check(Status.BAD_REQUEST, e.getReasonPhrase());
      TestMgr.check(e.getErrorData().toString().contains("propertyPath=errorCode.request.members"), true);
    }

    String strResult = template.getForObject(urlPrefix + "/validateQuery?name=", String.class);
    TestMgr.check(strResult, "");

    try {
      template.getForObject(urlPrefix + "/validateQuery", String.class);
      TestMgr.check(false, true);
    } catch (InvocationException e) {
      TestMgr.check(400, e.getStatus().getStatusCode());
      TestMgr.check(Status.BAD_REQUEST, e.getReasonPhrase());
      TestMgr.check(e.getErrorData().toString().contains("propertyPath=queryValidate.name"), true);
    }
  }
}
