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
package org.apache.servicecomb.it.schema;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.ws.Holder;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestSchema(schemaId = "generic")
@RequestMapping(path = "/v1/generic")
public class GenericSchema {

  @PostMapping(path = "holderUser")
  public Holder<User> holderUser(@RequestBody Holder<User> input) {
    Assert.isInstanceOf(Holder.class, input);
    Assert.isInstanceOf(User.class, input.value);
    return input;
  }

  @PostMapping(path = "genericUser")
  public Generic<User> genericUser(@RequestBody Generic<User> input) {
    Assert.isInstanceOf(Generic.class, input);
    Assert.isInstanceOf(User.class, input.value);
    return input;
  }

  @PostMapping(path = "genericLong")
  public Generic<Long> genericLong(@RequestBody Generic<Long> input) {
    Assert.isInstanceOf(Generic.class, input);
    Assert.isInstanceOf(Long.class, input.value);
    return input;
  }

  @PostMapping(path = "genericDate")
  public Generic<Date> genericDate(@RequestBody Generic<Date> input) {
    Assert.isInstanceOf(Generic.class, input);
    Assert.isInstanceOf(Date.class, input.value);
    return input;
  }

  @PostMapping(path = "genericEnum")
  public Generic<HttpStatus> genericEnum(@RequestBody Generic<HttpStatus> input) {
    Assert.isInstanceOf(Generic.class, input);
    Assert.isInstanceOf(HttpStatus.class, input.value);
    return input;
  }

  @PostMapping(path = "genericGenericUser")
  public Generic<Generic<User>> genericGenericUser(@RequestBody Generic<Generic<User>> input) {
    Assert.isInstanceOf(Generic.class, input);
    Assert.isInstanceOf(Generic.class, input.value);
    Assert.isInstanceOf(User.class, input.value.value);
    return input;
  }

  @PostMapping(path = "genericMap")
  public Generic<Map<String, String>> genericMap(@RequestBody Generic<Map<String, String>> mapGeneric) {
    Assert.isInstanceOf(Generic.class, mapGeneric);
    Assert.isInstanceOf(Map.class, mapGeneric.value);
    return mapGeneric;
  }

  @PostMapping(path = "genericMapList")
  public Generic<Map<String, List<String>>> genericMapList(
      @RequestBody Generic<Map<String, List<String>>> mapListGeneric) {
    Assert.isInstanceOf(Generic.class, mapListGeneric);
    Assert.isInstanceOf(Map.class, mapListGeneric.value);
    return mapListGeneric;
  }

  @PostMapping(path = "genericMapListUser")
  public Generic<Map<String, List<User>>> genericMapListUser(
      @RequestBody Generic<Map<String, List<User>>> mapListUserGeneric) {
    Assert.isInstanceOf(Generic.class, mapListUserGeneric);
    Assert.isInstanceOf(Map.class, mapListUserGeneric.value);
    return mapListUserGeneric;
  }

  @PostMapping(path = "genericNestedListString")
  public List<List<String>> genericNestedListString(@RequestBody List<List<String>> nestedListString) {
    Assert.isInstanceOf(List.class, nestedListString);
    Assert.isInstanceOf(List.class, nestedListString.get(0));
    Assert.isInstanceOf(String.class, nestedListString.get(0).get(0));
    return nestedListString;
  }

  @PostMapping(path = "genericNestedListUser")
  public List<List<User>> genericNestedListUser(@RequestBody List<List<User>> nestedListUser) {
    Assert.isInstanceOf(List.class, nestedListUser);
    Assert.isInstanceOf(List.class, nestedListUser.get(0));
    Assert.isInstanceOf(User.class, nestedListUser.get(0).get(0));
    return nestedListUser;
  }
}
