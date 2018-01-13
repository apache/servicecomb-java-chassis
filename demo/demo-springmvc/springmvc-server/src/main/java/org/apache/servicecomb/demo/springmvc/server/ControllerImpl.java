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

package org.apache.servicecomb.demo.springmvc.server;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.demo.controller.Person;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RestSchema(schemaId = "controller")
@RequestMapping(path = "/springmvc/controller", produces = MediaType.APPLICATION_JSON)
public class ControllerImpl {
  @GetMapping(path = "/add")
  public int add(@RequestParam("a") int a, @RequestParam("b") int b) {
    return a + b;
  }

  @PostMapping(path = "/sayhello/{name}")
  public String sayHello(@PathVariable("name") String name) {
    return "hello " + name;
  }

  @RequestMapping(path = "/saysomething", method = RequestMethod.POST)
  public String saySomething(String prefix, @RequestBody Person user) {
    return prefix + " " + user.getName();
  }

  @RequestMapping(path = "/sayhi", method = RequestMethod.GET)
  public String sayHi(HttpServletRequest request) throws Exception {
    String addr = request.getRemoteAddr();
    if (addr == null || addr.isEmpty()) {
      throw new Exception("Can't get remote addr!");
    }
    String[] values = request.getParameterValues("name");
    if (values != null && values.length > 0 && values[0].equals("throwexception")) {
      throw new RuntimeException();
    }
    return "hi " + request.getParameter("name") + " " + Arrays.toString(values);
  }

  @RequestMapping(path = "/sayhei", method = RequestMethod.GET)
  public String sayHei(@RequestHeader("name") String name) {
    return "hei " + name;
  }
}
