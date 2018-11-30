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
package org.apache.servicecomb.codec.protobuf.internal.converter.model;


import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.apache.servicecomb.foundation.test.scaffolding.model.Empty;
import org.apache.servicecomb.foundation.test.scaffolding.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RequestMapping(path = "/")
public class ProtoSchema {
  @ApiResponses(value = {@ApiResponse(code = 444, response = Color.class, message = "xxx")})
  @GetMapping(path = "/base")
  public int base(boolean boolValue, int iValue, long lValue, float fValue, double dValue, String sValue, int[] iArray,
      Color color,
      LocalDate localDate, Date date, @RequestBody Empty empty) {
    return 0;
  }

  @GetMapping(path = "/bytes")
  public byte[] bytes(@RequestBody byte[] value) {
    return null;
  }

  @GetMapping(path = "/colorBody")
  public Color colorBody(@RequestBody Color color) {
    return null;
  }

  @GetMapping(path = "/obj")
  public Object obj(@RequestBody Object value) {
    return null;
  }

  @GetMapping(path = "/user")
  public User user(@RequestBody User user) {
    return null;
  }

  @GetMapping(path = "/userWrapInProtobuf")
  public User userWrapInProtobuf(@RequestBody User user, int ivalue) {
    return null;
  }

  @GetMapping(path = "/listObj")
  public List<Object> listObj(@RequestBody List<Object> objs) {
    return null;
  }

  @GetMapping(path = "/listUser")
  public List<User> listUser(@RequestBody List<User> users) {
    return null;
  }

  @GetMapping(path = "/mapUser")
  public Map<String, User> mapUser(@RequestBody Map<String, User> users) {
    return null;
  }

  @GetMapping(path = "/mapObj")
  public Map<String, Object> mapObj(@RequestBody Map<String, Object> objs) {
    return null;
  }

  @GetMapping(path = "/ref")
  public Ref2 ref(@RequestBody Ref1 ref) {
    return null;
  }

  @GetMapping(path = "/noParamVoid")
  public void noParamVoid() {
  }
}
