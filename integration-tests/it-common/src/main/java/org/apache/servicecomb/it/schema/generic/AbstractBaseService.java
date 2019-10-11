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

import io.swagger.annotations.ApiOperation;

public class AbstractBaseService<T extends AbstractBean> implements IBaseService<T> {
  private IBaseService<T> target;

  protected AbstractBaseService(IBaseService<T> t) {
    target = t;
  }

  @Override
  @ApiOperation(nickname = "hello", value = "hello")
  public T hello(T a) {
    return target.hello(a);
  }

  @Override
  @ApiOperation(nickname = "helloWithValue", value = "helloWithValue")
  public T hello(T a, String value) {
    return target.hello(a, value);
  }

  @Override
  public T[] helloBody(T[] a) {
    return target.helloBody(a);
  }

  @Override
  public List<T> helloList(List<T> a) {
    return a;
  }

  @Override
  public PersonBean actual() {
    return target.actual();
  }

  @Override
  public PersonBean objectParam(Object obj) {
    return target.objectParam(obj);
  }

  @Override
  public PersonBean objectParamTwo(Object obj, String name) {
    return target.objectParamTwo(obj, name);
  }
}
