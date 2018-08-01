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

package org.apache.servicecomb.bizkeeper;

import org.apache.servicecomb.core.Invocation;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixKey;
import com.netflix.hystrix.util.InternMap;

/**
 * 通过定制CommandGroupKey，目的是携带Invocation部分静态信息，便于CircutBreakerEvent获取
 */
public class CustomizeCommandGroupKey extends HystrixKey.HystrixKeyDefault implements HystrixCommandGroupKey {

  private Invocation instance;

  public CustomizeCommandGroupKey(String key) {
    super(key);
  }

  private static final InternMap<String, CustomizeCommandGroupKey> intern =
      new InternMap<String, CustomizeCommandGroupKey>(
          new InternMap.ValueConstructor<String, CustomizeCommandGroupKey>() {
            @Override
            public CustomizeCommandGroupKey create(String key) {
              return new CustomizeCommandGroupKey(key);
            }
          });

  public static HystrixCommandGroupKey asKey(String key, Invocation invocation) {
    CustomizeCommandGroupKey result = intern.interned(key);
    result.setInvocation(invocation);
    return result;
  }

  public void setInvocation(Invocation invocation) {
    this.instance = invocation;
  }

  public Invocation getInstance() {
    return instance;
  }
}
