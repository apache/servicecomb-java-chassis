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

package org.apache.servicecomb.demo.pojo.server;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.demo.CodeFirstPojoIntf;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.demo.mapnull.ParseRequest;
import org.apache.servicecomb.demo.mapnull.ParseResponse;
import org.apache.servicecomb.demo.server.MapModel;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.provider.pojo.RpcSchema;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;

@RpcSchema()
@SwaggerDefinition(basePath = "/pojo/rest")
public class CodeFirstPojo implements CodeFirstPojoIntf {
  @Override
  public ParseResponse parse(ParseRequest request) {
    ParseResponse r = new ParseResponse();
    r.setResultCode("CU1I0000");
    r.setResultInfo("报文处理成功！报文解析成功！");
    r.setMsgType("cncc.111.001.01");
    Map<String, String> h = new HashMap<>();
    h.put("C11", "20200101");
    h.put("F41", "3");
    h.put("F40", "cncc.111.001.01");
    h.put("E24", "HVPA5C30177808463743");
    h.put("E35", "HVPA5C30177808463743");
    h.put("A29", "{N:, K20=}");
    h.put("F38", "HVPS");
    h.put("K13", "01");
    h.put("F39", "SAPS");
    h.put("K14", "CMT");
    h.put("K16", "");
    h.put("C92", "102633");
    r.setMsgHeader(h);
    Map<String, Object> b = new HashMap<>();
    b.put("E50", "hvps.141.001.01");
    b.put("A00", "402451000010");
    b.put("A01", "105100000017");
    b.put("F32", "NORM");
    b.put("E57", "19218385");
    b.put("D14", "200000.00");
    b.put("C14", "20200101");
    b.put("F25", "02711");
    b.put("A70", "908100000002");
    b.put("C92", "20200101");
    b.put("F2H", "G105");
    r.setMsgBody(b);
    return r;
  }

  @Override
  public MapModel testMapModel(MapModel model) {
    return model;
  }

  @Override
  public Map<String, String> testMap(Map<String, String> map) {
    return map;
  }

  @Override
  public Map<String, User> testUserMap(Map<String, User> userMap) {
    return userMap;
  }

  @Override
  public List<User> testUserArray(List<User> users) {
    return users;
  }

  public String[] testStrings(String[] input) {
    input[0] += input[0] + "0";
    return input;
  }

  public byte[] testBytes(byte[] input) {
    input[0] = (byte) (input[0] + 1);
    return input;
  }

  public int reduce(int a, int b) {
    return a - b;
  }

  public Date addDate(Date date, long second) {
    return new Date(date.getTime() + second * 1000);
  }

  public Person sayHello(Person user) {
    user.setName("hello " + user.getName());
    return user;
  }

  public String saySomething(String prefix, Person user) {
    return prefix + " " + user.getName();
  }

  public String sayHi(String name) {
    ContextUtils.getInvocationContext().setStatus(202);
    return name + " sayhi, context k: "
        + (ContextUtils.getInvocationContext() == null ? ""
        : ContextUtils.getInvocationContext().getContext("k"));
  }

  @ApiOperation(nickname = "sayHi2", value = "")
  public CompletableFuture<String> sayHi2Async(String name) {
    CompletableFuture<String> future = new CompletableFuture<>();
    future.complete(name + " sayhi 2");
    return future;
  }

  public boolean isTrue() {
    return true;
  }

  public String addString(List<String> s) {
    StringBuilder result = new StringBuilder();
    for (String x : s) {
      result.append(x);
    }
    return result.toString();
  }
}
