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

package org.apache.servicecomb.swagger.generator.core.utils.paramUtilsModel;

import java.util.List;

public class AbstractBaseService<T extends AbstractBean> implements IBaseService<T> {
  private final IBaseService<T> target;

  protected AbstractBaseService(IBaseService<T> t) {
    target = t;
  }

  @Override
  public T hello(T a) {
    return target.hello(a);
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
  public PersonBean actual(PersonBean bean) {
    return target.actual(bean);
  }
}
