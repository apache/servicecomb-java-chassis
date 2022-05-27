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

package org.apache.servicecomb.core.invocation.endpoint;

import org.apache.servicecomb.core.Endpoint;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public final class EndpointCacheUtils {
  private static final LoadingCache<String, Endpoint> CACHE = CacheBuilder.newBuilder()
      .maximumSize(10000)
      .build(new CacheLoader<String, Endpoint>() {
        @Override
        public Endpoint load(String uri) {
          return EndpointCacheUtils.create(uri);
        }
      });

  /**
   * @param uri https://www.abc.com:12345
   * @return endpoint
   */
  public static Endpoint getOrCreate(String uri) {
    return CACHE.getUnchecked(uri);
  }

  public static Endpoint create(String uri) {
    return EndpointUtils.parse(EndpointUtils.formatFromUri(uri));
  }
}
