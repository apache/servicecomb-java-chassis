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
package org.apache.servicecomb.loadbalance;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.List;

import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.netflix.client.DefaultLoadBalancerRetryHandler;
import com.netflix.client.RetryHandler;
import com.netflix.client.Utils;

@Component
public class DefaultRetryExtensionsFactory implements ExtensionsFactory {
  private static final Collection<String> ACCEPT_KEYS = Lists.newArrayList(
      Configuration.PROP_RETRY_HANDLER);

  private static final String RETRY_DEFAULT = "default";

  private static final Collection<String> ACCEPT_VALUES = Lists.newArrayList(
      RETRY_DEFAULT);

  @Override
  public boolean isSupport(String key, String value) {
    return ACCEPT_KEYS.contains(key) && ACCEPT_VALUES.contains(value);
  }

  @SuppressWarnings("unchecked")
  public RetryHandler createRetryHandler(String retryName, String microservice) {
    return new DefaultLoadBalancerRetryHandler(
        Configuration.INSTANCE.getRetryOnSame(microservice),
        Configuration.INSTANCE.getRetryOnNext(microservice), true) {
      private List<Class<? extends Throwable>> retriable = Lists
          .newArrayList(new Class[] {ConnectException.class, SocketTimeoutException.class});

      @Override
      public boolean isRetriableException(Throwable e, boolean sameServer) {
        boolean retriable = Utils.isPresentAsCause(e, getRetriableExceptions());
        if (!retriable) {
          if (e instanceof InvocationException) {
            if (((InvocationException) e).getStatusCode() == 503) {
              return true;
            }
          }
        }
        return retriable;
      }

      @Override
      protected List<Class<? extends Throwable>> getRetriableExceptions() {
        return this.retriable;
      }
    };
  }
}
