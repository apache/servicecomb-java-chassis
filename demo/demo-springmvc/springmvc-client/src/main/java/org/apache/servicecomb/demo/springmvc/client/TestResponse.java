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

import java.util.Date;
import java.util.Objects;

import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.compute.GenericParam;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

public class TestResponse {
  private CodeFirstSpringmvcIntf intf;

  public TestResponse() {
    intf = Invoker.createProxy("springmvc", "codeFirst", CodeFirstSpringmvcIntf.class);
  }

  public void runRest() {
    checkQueryGenericObject();
    checkQueryGenericString();
    testDelay();
    testAbort();
    testDecodeResponseError();
    checkQueryObject();
    testCseResponse();
    testResponseEntity();
    testCseResponseCorrect();
  }

  public void runHighway() {
  }

  public void runAllTransport() {
    testvoidResponse();
    testVoidResponse();
    checkQueryObject();
    testCseResponse();
    testResponseEntity();
  }

  private void testCseResponse() {
    String srcName = RegistrationManager.INSTANCE.getMicroservice().getServiceName();
    Response cseResponse = intf.cseResponse();
    TestMgr.check("User [name=nameA, age=100, index=0]", cseResponse.getResult());
    TestMgr.check("h1v " + srcName, cseResponse.getHeaders().getFirst("h1"));
    TestMgr.check("h2v " + srcName, cseResponse.getHeaders().getFirst("h2"));
    TestMgr.check(cseResponse.getStatusCode(), 202);
  }

  private void testCseResponseCorrect() {
    String srcName = RegistrationManager.INSTANCE.getMicroservice().getServiceName();
    Response cseResponse = intf.cseResponseCorrect();
    TestMgr.check("User [name=nameA, age=100, index=0]", cseResponse.getResult());
    TestMgr.check("h1v " + srcName, cseResponse.getHeaders().getFirst("h1"));
    TestMgr.check("h2v " + srcName, cseResponse.getHeaders().getFirst("h2"));
    TestMgr.check(cseResponse.getStatusCode(), 202);
  }

  private void testResponseEntity() {
    Date date = new Date();

    String srcName = RegistrationManager.INSTANCE.getMicroservice().getServiceName();

    ResponseEntity<Date> responseEntity = intf.responseEntity(date);
    TestMgr.check(date, responseEntity.getBody());
    TestMgr.check("h1v " + srcName, responseEntity.getHeaders().getFirst("h1"));
    TestMgr.check("h2v " + srcName, responseEntity.getHeaders().getFirst("h2"));

    TestMgr.check(202, responseEntity.getStatusCodeValue());
  }

  private void testvoidResponse() {
    intf.testvoidInRPC();
  }

  private void testVoidResponse() {
    intf.testVoidInRPC();
  }

  private void checkQueryObject() {
    String result = intf.checkQueryObject("name1", "otherName2", new Person("bodyName"));
    TestMgr.check("invocationContext_is_null=false,person=name1,otherName=otherName2,name=name1,requestBody=bodyName",
        result);
  }

  private void checkQueryGenericObject() {
    GenericParam<Person> requestBody = new GenericParam<>();
    requestBody.num(1).str("str1").setData(new Person("bodyPerson"));
    String result = intf.checkQueryGenericObject(requestBody, "str2", 2);
    TestMgr.check(
        "str=str2,generic=GenericParamWithJsonIgnore{str='str2', num=2, data=null},requestBody=GenericParam{str='str1', num=1, data=bodyPerson}",
        result);
  }

  private void checkQueryGenericString() {
    GenericParam<Person> requestBody = new GenericParam<>();
    requestBody.num(1).str("str1").setData(new Person("bodyPerson"));
    String result = intf.checkQueryGenericString("str2", requestBody, 2, "dataTest", "strInSubclass", 33);
    TestMgr.check(
        "str=str2,generic=GenericParamExtended{strExtended='strInSubclass', intExtended=33, super="
            + "GenericParam{str='str2', num=2, data=dataTest}},requestBody=GenericParam{str='str1', num=1, data=bodyPerson}",
        result);
  }

  private void testDelay() {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < 4; ++i) {
      result.append(intf.testDelay()).append("|");
    }
    TestMgr.check("OK|OK|OK|OK|", result.toString());
  }

  private void testAbort() {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < 4; ++i) {
      String response;
      try {
        response = intf.testAbort();
      } catch (InvocationException e) {
        response = e.getMessage();
      }
      result.append(response).append("|");
    }
    TestMgr.check(
        "OK|InvocationException: code=421;msg=CommonExceptionData [message=aborted by fault inject]|"
            + "OK|InvocationException: code=421;msg=CommonExceptionData [message=aborted by fault inject]|",
        result.toString());
  }

  private void testDecodeResponseError() {
    InvocationException exception = null;
    try {
      intf.testDecodeResponseError();
    } catch (InvocationException e) {
      // 1. InvocationException: wrapper exception
      exception = e;
    }
    Objects.requireNonNull(exception);
    // 2. CseException: bizKeeper exception
    Throwable cause = exception.getCause();
    TestMgr.check(InvalidFormatException.class, cause.getClass());
    TestMgr.check(
        ((InvalidFormatException) cause).getMessage().contains("Cannot deserialize value of type `java.util.Date`"),
        true);
  }
}
