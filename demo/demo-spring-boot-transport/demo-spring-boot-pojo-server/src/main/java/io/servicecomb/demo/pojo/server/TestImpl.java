/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.demo.pojo.server;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.servicecomb.demo.server.Test;
import io.servicecomb.demo.server.TestRequest;
import io.servicecomb.demo.server.User;
import io.servicecomb.provider.pojo.RpcSchema;
import io.servicecomb.swagger.invocation.exception.InvocationException;

@RpcSchema(schemaId = "server")
@RequestMapping(path = "/pojo/rest", produces = MediaType.APPLICATION_JSON)
public class TestImpl implements Test {
  @RequestMapping(path = "/testStringArray", method = RequestMethod.GET)
  @ResponseBody
  @Override
  public String testStringArray(String[] arr) {
    return String.format("arr is '%s'", Arrays.toString(arr));
  }

  @RequestMapping(path = "/testStatic", method = RequestMethod.GET)
  @ResponseBody
  @Override
  public String getTestString(String code) {
    return String.format("code is '%s'", String.valueOf(code));
  }

  @RequestMapping(path = "/testStatic", method = RequestMethod.POST)
  @ResponseBody
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

  @RequestMapping(path = "/testException", method = RequestMethod.GET)
  @ResponseBody
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

  @RequestMapping(path = "/splitParam", method = RequestMethod.POST)
  @ResponseBody
  @Override
  public User splitParam(@RequestParam(name = "index") int index, @RequestBody User user) {
    return doTest(index, user, null, null);
  }

  @RequestMapping(path = "/wrapParam", method = RequestMethod.POST)
  @ResponseBody
  @Override
  public User wrapParam(@RequestBody TestRequest request) {
    if (request == null) {
      return null;
    }
    return doTest(request.getIndex(), request.getUser(), request.getUsers(), request.getData());
  }

  @RequestMapping(path = "/addstring", method = RequestMethod.DELETE)
  @ResponseBody
  @Override
  public String addString(String[] strArr) {
    String result = Arrays.toString(strArr);
    System.out.println("addString: " + result);
    return result;
  }
}
