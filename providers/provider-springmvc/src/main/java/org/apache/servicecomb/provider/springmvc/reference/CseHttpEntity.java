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
package org.apache.servicecomb.provider.springmvc.reference;

import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;

public class CseHttpEntity<T> extends HttpEntity<T> {

  private InvocationContext context;

  public CseHttpEntity(T body) {
    super(body);
  }

  public CseHttpEntity(MultiValueMap<String, String> headers) {
    super(headers);
  }

  public CseHttpEntity(T body, MultiValueMap<String, String> headers) {
    super(body, headers);
  }

  /**
   * 获取context的值
   * @return 返回 context
   */
  public InvocationContext getContext() {
    return context;
  }

  /**
   * 对context进行赋值
   * @param context context的新值
   */
  public void setContext(InvocationContext context) {
    this.context = context;
  }

  public void addContext(String key, String value) {
    if (context == null) {
      context = new InvocationContext();
    }

    context.addContext(key, value);
  }
}
