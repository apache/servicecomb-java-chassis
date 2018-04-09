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

package org.apache.servicecomb.demo.validator.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.demo.DemoConst;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class CodeFirstValidatorRestTemplate {
  protected void changeTransport(String microserviceName, String transport) {
    CseContext.getInstance().getConsumerProviderManager().setTransport(microserviceName, transport);
    TestMgr.setMsg(microserviceName, transport);
  }

  public void testCodeFirst(RestTemplate template, String microserviceName, String basePath) {
    String cseUrlPrefix = "cse://" + microserviceName + basePath;
    for (String transport : DemoConst.transports) {
      changeTransport(microserviceName, transport);
      testAllTransport(microserviceName, template, cseUrlPrefix);
    }
  }

  protected void testAllTransport(String microserviceName, RestTemplate template, String cseUrlPrefix) {
    testCodeFirstAdd(template, cseUrlPrefix);
    testCodeFirstAddForException(template, cseUrlPrefix);
    testCodeFirstSayHi(template, cseUrlPrefix);
    testCodeFirstSayHiForException(template, cseUrlPrefix);
    testTraceIdOnContextContainsTraceId(template, cseUrlPrefix);
  }

  protected void checkStatusCode(String microserviceName, int expectStatusCode, HttpStatus httpStatus) {
    TestMgr.check(expectStatusCode, httpStatus.value());
  }

  protected void testCodeFirstSayHi(RestTemplate template, String cseUrlPrefix) {
    ResponseEntity<String> responseEntity =
        template.exchange(cseUrlPrefix + "sayhi/{name}", HttpMethod.PUT, null, String.class, "world");
    TestMgr.check(202, responseEntity.getStatusCode());
    TestMgr.check("world sayhi", responseEntity.getBody());
  }

  protected void testCodeFirstSayHiForException(RestTemplate template, String cseUrlPrefix) {
    boolean isExcep = false;
    try {
      template.exchange(cseUrlPrefix + "sayhi/{name}", HttpMethod.PUT, null, String.class, "te");
    } catch (Exception e) {
      isExcep = true;
    }
    TestMgr.check(true, isExcep);
  }

  protected void testCodeFirstAdd(RestTemplate template, String cseUrlPrefix) {
    Map<String, String> params = new HashMap<>();
    params.put("a", "5");
    params.put("b", "20");
    int result =
        template.postForObject(cseUrlPrefix + "add", params, Integer.class);
    TestMgr.check(25, result);
  }

  protected void testCodeFirstAddForException(RestTemplate template, String cseUrlPrefix) {
    Map<String, String> params = new HashMap<>();
    params.put("a", "5");
    params.put("b", "3");
    boolean isExcep = false;
    try {
      template.postForObject(cseUrlPrefix + "add", params, Integer.class);
    } catch (Exception e) {
      isExcep = true;
    }

    TestMgr.check(true, isExcep);
  }

  protected void testTraceIdOnNotSetBefore(RestTemplate template, String cseUrlPrefix) {
    String traceIdUrl = cseUrlPrefix + "traceId";
    String result = template.getForObject(traceIdUrl, String.class);
    TestMgr.checkNotEmpty(result);
  }

  protected void testTraceIdOnContextContainsTraceId(RestTemplate template, String cseUrlPrefix) {
    String traceIdUrl = cseUrlPrefix + "traceId";
    InvocationContext invocationContext = new InvocationContext();
    invocationContext.addContext(Const.TRACE_ID_NAME, String.valueOf(Long.MIN_VALUE));
    ContextUtils.setInvocationContext(invocationContext);
    String result = template.getForObject(traceIdUrl, String.class);
    TestMgr.check(String.valueOf(Long.MIN_VALUE), result);
    ContextUtils.removeInvocationContext();
  }
}
