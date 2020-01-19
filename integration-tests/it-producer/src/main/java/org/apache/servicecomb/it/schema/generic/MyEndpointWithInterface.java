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
package org.apache.servicecomb.it.schema.generic;

import java.util.List;

import org.apache.servicecomb.provider.pojo.RpcSchema;

import io.swagger.annotations.ApiOperation;

// TODO : WEAK RPC & highway both not support this now , need fix it
//@RpcSchema(schemaId = "MyEndpointWithInterface")
public class MyEndpointWithInterface implements IMyService {
  @Override
  @ApiOperation(nickname = "hello", value = "hello")
  public PersonBean hello(PersonBean a) {
    return a;
  }

  @Override
  @ApiOperation(nickname = "helloWithValue", value = "helloWithValue")
  public PersonBean hello(PersonBean a, String value) {
    a.setName(a.getName() + ":" + value);
    return a;
  }

  @Override
  public PersonBean[] helloBody(PersonBean[] a) {
    return a;
  }

  @Override
  public List<PersonBean> helloList(List<PersonBean> a) {
    return a;
  }

  @Override
  public PersonBean actual() {
    PersonBean p = new PersonBean();
    p.setName("p");
    return p;
  }

  @Override
  public PersonBean objectParam(Object obj) {
    PersonBean p = new PersonBean();
    p.setName(obj.toString());
    return p;
  }

  @Override
  public PersonBean objectParamTwo(Object obj, String name) {
    PersonBean p = new PersonBean();
    p.setName(obj.toString() + ":" + name);
    return p;
  }
}
