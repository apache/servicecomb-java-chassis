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
package org.apache.servicecomb.springboot.springmvc.client;

import org.apache.http.HttpStatus;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.controller.Person;
import org.apache.servicecomb.provider.springmvc.reference.CseRestTemplate;
import org.apache.servicecomb.provider.springmvc.reference.UrlWithServiceNameClientHttpRequestFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TestAnnotationsSchema implements CategorizedTestCase {
  private static final String microserviceName = "springmvc";

  private static final RestTemplate templateUrlWithServiceName = new CseRestTemplate();

  @Override
  public void testRestTransport() throws Exception {
    templateUrlWithServiceName.setRequestFactory(new UrlWithServiceNameClientHttpRequestFactory());
    testRequiredBody(templateUrlWithServiceName, microserviceName);
    testRegExpPath();
  }

  private void testRegExpPath() {
    String prefix = "cse://" + microserviceName;
    String result = templateUrlWithServiceName.getForObject(prefix + "/annotations/testRegExpPath/a?name={name}",
        String.class, "a");
    TestMgr.check("a", result);
    result = templateUrlWithServiceName.getForObject(prefix + "/annotations/testRegExpPath/a/b?name={name}",
        String.class, "ab");
    TestMgr.check("ab", result);
    result = templateUrlWithServiceName.getForObject(prefix + "/annotations/testRegExpPath/a/b/c?name={name}",
        String.class, "abc");
    TestMgr.check("abc", result);
  }

  private static void testRequiredBody(RestTemplate template, String microserviceName) {
    String prefix = "cse://" + microserviceName;
    Person user = new Person();

    TestMgr.check("No user data found",
        template.postForObject(prefix + "/annotations/saysomething?prefix={prefix}",
            user,
            String.class,
            "ha"));

    user.setName("world");
    TestMgr.check("ha world",
        template.postForObject(prefix + "/annotations/saysomething?prefix={prefix}",
            user,
            String.class,
            "ha"));

    TestMgr.check("No user data found",
        template.postForObject(prefix + "/annotations/saysomething?prefix={prefix}",
            null,
            String.class,
            "ha"));

    TestMgr.check("No user name found",
        template.postForObject(prefix + "/annotations/say",
            "",
            String.class,
            "ha"));
    TestMgr.check("test",
        template.postForObject(prefix + "/annotations/say",
            "test",
            String.class,
            "ha"));

    try {
      template.postForObject(prefix + "/annotations/testRequiredBody",
          null,
          String.class);
      TestMgr.fail("should fail");
    } catch (InvocationException e) {
      TestMgr.check(e.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
    }
  }
}
