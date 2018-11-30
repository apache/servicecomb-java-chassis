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

import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.compute.GenericParam;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

public class TestRestTemplate {
  private RestTemplate restTemplate = RestTemplateBuilder.create();

  public void runAllTest() {
    testvoidResponse();
    testVoidResponse();
    checkAllVoidTestResult();
    checkQueryObject();
  }

  public void runRest() {
    checkQueryGenericObject();
    checkQueryGenericString();
  }

  private void testvoidResponse() {
    final ResponseEntity<Void> resultEntity = restTemplate
        .getForEntity("cse://springmvc/codeFirstSpringmvc/testvoidInRestTemplate", void.class);
    Assert.isTrue(200 == resultEntity.getStatusCodeValue(), "void return type invocation failed");
  }

  private void testVoidResponse() {
    final ResponseEntity<Void> resultEntity = restTemplate
        .getForEntity("cse://springmvc/codeFirstSpringmvc/testVoidInRestTemplate", Void.class);
    Assert.isTrue(200 == resultEntity.getStatusCodeValue(), "Void return type invocation failed");
  }

  private void checkAllVoidTestResult() {
    final ResponseEntity<Boolean> resultEntity = restTemplate
        .getForEntity("cse://springmvc/codeFirstSpringmvc/checkVoidResult", boolean.class);
    Assert.isTrue(resultEntity.getBody(), "not all void test is passed");
  }

  private void checkQueryObject() {
    final ResponseEntity<String> responseEntity = restTemplate
        .postForEntity("cse://springmvc/codeFirstSpringmvc/checkQueryObject?name={1}&otherName={2}",
            new Person("bodyName"), String.class, "name1", "otherName2");
    TestMgr.check("invocationContext_is_null=false,person=name1,otherName=otherName2,name=name1,requestBody=bodyName",
        responseEntity.getBody());
  }

  private void checkQueryGenericObject() {
    final GenericParam<Person> requestBody = new GenericParam<>();
    requestBody.setNum(1).setStr("str1").setData(new Person("bodyPerson"));
    final HttpEntity<GenericParam<Person>> requestEntity = new HttpEntity<>(requestBody);
    final ResponseEntity<String> responseEntity = restTemplate
        .exchange("cse://springmvc/codeFirstSpringmvc/checkQueryGenericObject?str={1}&num={2}",
            HttpMethod.PUT, requestEntity, String.class, "str2", 2);
    TestMgr.check(
        "str=str2,generic=GenericParamWithJsonIgnore{str='str2', num=2, data=null},requestBody=GenericParam{str='str1', num=1, data=bodyPerson}",
        responseEntity.getBody());
  }

  private void checkQueryGenericString() {
    final GenericParam<Person> requestBody = new GenericParam<>();
    requestBody.setNum(1).setStr("str1").setData(new Person("bodyPerson"));
    final ResponseEntity<String> responseEntity = restTemplate.exchange(
        "cse://springmvc/codeFirstSpringmvc/checkQueryGenericString?str={1}&num={2}&data={3}&strExtended={4}&intExtended={5}",
        HttpMethod.PUT, new HttpEntity<>(requestBody), String.class, "str2", 2, "dataTest",
        "strInSubclass", 33);
    TestMgr.check(
        "str=str2,generic=GenericParamExtended{strExtended='strInSubclass', intExtended=33, super="
            + "GenericParam{str='str2', num=2, data=dataTest}},requestBody=GenericParam{str='str1', num=1, data=bodyPerson}",
        responseEntity.getBody());
  }
}
