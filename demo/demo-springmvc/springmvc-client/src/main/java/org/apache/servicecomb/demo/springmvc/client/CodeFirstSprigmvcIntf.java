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

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.ws.Holder;

import org.apache.servicecomb.demo.EmptyObject;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.swagger.invocation.Response;
import org.springframework.http.ResponseEntity;

public interface CodeFirstSprigmvcIntf {
  ResponseEntity<Date> responseEntity(Date date);

  Response cseResponse();

  Object testObject(Object input);

  EmptyObject testEmpty(EmptyObject input);

  Map<String, Object> testMapObject(Map<String, Object> input);

  List<Object> testListObject(List<Object> input);

  Holder<Object> testHolderObject(Holder<Object> input);

  Holder<User> testGeneric(Holder<User> input);
}
