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

public interface ProtoSchemaIntf {
  int base(boolean boolValue, int iValue, long lValue, float fValue, double dValue, String sValue, int[] iArray,
      Color color,
      LocalDate localDate, Date date, Empty empty);

  byte[] bytes(byte[] value);


  Color colorBody(Color color);


  Object obj(Object value);


  User user(User user);

  User userWrapInProtobuf(User user, int ivalue);


  List<Object> listObj(List<Object> objs);

  List<User> listUser(List<User> users);


  Map<String, User> mapUser(Map<String, User> users);

  Map<String, Object> mapObj(Map<String, Object> objs);

  Ref2 ref(Ref1 ref);


  void noParamVoid();

  List<List<String>> listListString(List<List<String>> value);


  List<List<User>> listListUser(List<List<User>> value);


  List<Map<String, String>> listMapString(List<Map<String, String>> value);


  List<Map<String, User>> listMapUser(List<Map<String, User>> value);


  Map<String, List<String>> mapListString(Map<String, List<String>> value);


  Map<String, List<User>> mapListUser(Map<String, List<User>> value);

  Map<String, Map<String, String>> mapMapString(Map<String, Map<String, String>> value);


  Map<String, Map<String, User>> mapMapUser(Map<String, Map<String, User>> value);


  List<List<List<String>>> listListListString(List<List<List<String>>> value);


  List<List<Map<String, String>>> listListMapString(List<List<Map<String, String>>> value);

  List<Map<String, List<String>>> listMapListString(List<Map<String, List<String>>> value);


  List<Map<String, Map<String, String>>> listMapMapString(
      List<Map<String, Map<String, String>>> value);


  Map<String, Map<String, List<String>>> mapMapListString(
      Map<String, Map<String, List<String>>> value);

  Map<String, Map<String, Map<String, String>>> mapMapMapString(
      Map<String, Map<String, Map<String, String>>> value);

  FieldNeedWrap fieldNeedWrap(FieldNeedWrap fieldNeedWrap);
}
