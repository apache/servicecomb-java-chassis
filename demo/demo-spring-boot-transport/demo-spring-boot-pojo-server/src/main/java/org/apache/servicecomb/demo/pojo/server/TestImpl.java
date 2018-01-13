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

import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.demo.server.Test;
import org.apache.servicecomb.demo.server.TestRequest;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.provider.pojo.RpcSchema;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

@RpcSchema(schemaId = "server")
public class TestImpl implements Test {
  @Override
  public String testStringArray(String[] arr) {
    return String.format("arr is '%s'", Arrays.toString(arr));
  }

  @Override
  public String getTestString(String code) {
    return String.format("code is '%s'", String.valueOf(code));
  }

  @Override
  public String postTestStatic(int code) {
    return null;
  }

  private User doTest(int index, User user, List<User> users, byte[] data) {
    if (user == null) {
      user = new User();
    }

    user.setIndex(index);

    int userCount = (users == null) ? 0 : users.size();
    user.setName(user.getName() + ",  users count:" + userCount);
    return user;
  }

  @Override
  public String testException(int code) {
    String strCode = String.valueOf(code);
    switch (code) {
      case 200:
        return strCode;
      case 456:
        throw new InvocationException(code, strCode, strCode + " error");
      case 556:
        throw new InvocationException(code, strCode, Arrays.asList(strCode + " error"));
      case 557:
        throw new InvocationException(code, strCode, Arrays.asList(Arrays.asList(strCode + " error")));
      default:
        break;
    }

    return "not expected";
  }

  @Override
  public User splitParam(int index, User user) {
    return doTest(index, user, null, null);
  }

  @Override
  public User wrapParam(TestRequest request) {
    if (request == null) {
      return null;
    }
    return doTest(request.getIndex(), request.getUser(), request.getUsers(), request.getData());
  }

  @Override
  public String addString(String[] strArr) {
    String result = Arrays.toString(strArr);
    System.out.println("addString: " + result);
    return result;
  }
}
