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

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public class ProtoSchemaPojo {
  @ApiResponses(value = {@ApiResponse(code = 444, response = Color.class, message = "xxx")})
  public int base(boolean boolValue, int iValue, long lValue, float fValue, double dValue, String sValue, int[] iArray,
      Color color,
      LocalDate localDate, Date date, Empty empty) {
    return 0;
  }

  public byte[] bytes(byte[] value) {
    return null;
  }


  public Color colorBody(Color color) {
    return null;
  }


  public Object obj(Object value) {
    return null;
  }


  public User user(User user) {
    return null;
  }


  public User userWrapInProtobuf(User user, int ivalue) {
    return null;
  }


  public List<Object> listObj(List<Object> objs) {
    return null;
  }

  public List<User> listUser(List<User> users) {
    return null;
  }


  public Map<String, User> mapUser(Map<String, User> users) {
    return null;
  }

  public Map<String, Object> mapObj(Map<String, Object> objs) {
    return null;
  }

  public Ref2 ref(Ref1 ref) {
    return null;
  }


  public void noParamVoid() {
  }


  public List<List<String>> listListString(List<List<String>> value) {
    return value;
  }


  public List<List<User>> listListUser(List<List<User>> value) {
    return value;
  }


  public List<Map<String, String>> listMapString(List<Map<String, String>> value) {
    return value;
  }


  public List<Map<String, User>> listMapUser(List<Map<String, User>> value) {
    return value;
  }


  public Map<String, List<String>> mapListString(Map<String, List<String>> value) {
    return value;
  }


  public Map<String, List<User>> mapListUser(Map<String, List<User>> value) {
    return value;
  }


  public Map<String, Map<String, String>> mapMapString(Map<String, Map<String, String>> value) {
    return value;
  }


  public Map<String, Map<String, User>> mapMapUser(Map<String, Map<String, User>> value) {
    return value;
  }


  public List<List<List<String>>> listListListString(List<List<List<String>>> value) {
    return value;
  }


  public List<List<Map<String, String>>> listListMapString(List<List<Map<String, String>>> value) {
    return value;
  }

  public List<Map<String, List<String>>> listMapListString(List<Map<String, List<String>>> value) {
    return value;
  }


  public List<Map<String, Map<String, String>>> listMapMapString(
      List<Map<String, Map<String, String>>> value) {
    return value;
  }


  public Map<String, Map<String, List<String>>> mapMapListString(
      Map<String, Map<String, List<String>>> value) {
    return value;
  }

  public Map<String, Map<String, Map<String, String>>> mapMapMapString(
      Map<String, Map<String, Map<String, String>>> value) {
    return value;
  }

  public FieldNeedWrap fieldNeedWrap(FieldNeedWrap fieldNeedWrap) {
    return fieldNeedWrap;
  }
}
