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
package org.apache.servicecomb.governance.handler.ext;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLHandshakeException;

import com.google.common.collect.ImmutableMap;

import io.vertx.core.VertxException;
import io.netty.handler.ssl.SslHandshakeTimeoutException;

public interface FailurePredictor {
  Map<Class<? extends Throwable>, List<String>> STRICT_RETRIABLE =
      ImmutableMap.<Class<? extends Throwable>, List<String>>builder()
          .put(ConnectException.class, Collections.emptyList())
          .put(SocketTimeoutException.class, Collections.emptyList())
          /*
           * deal with some special exceptions caused by the server side close the connection
           */
          .put(IOException.class, Collections.singletonList("Connection reset by peer"))
          .put(VertxException.class, Collections.singletonList("Connection was closed"))
          .put(NoRouteToHostException.class, Collections.emptyList())
          .put(SSLHandshakeException.class, Collections.emptyList())
          .put(SslHandshakeTimeoutException.class, Collections.emptyList())
          .build();

  boolean isFailedResult(List<String> statusList, Object result);

  default boolean isFailedResult(Throwable e) {
    return canRetryForException(STRICT_RETRIABLE, e);
  }

  static boolean canRetryForException(Map<Class<? extends Throwable>, List<String>> retryList,
      Throwable throwableToSearchIn) {
    // retry on exception type on message match
    int infiniteLoopPreventionCounter = 10;
    while (throwableToSearchIn != null && infiniteLoopPreventionCounter > 0) {
      infiniteLoopPreventionCounter--;
      for (Entry<Class<? extends Throwable>, List<String>> c : retryList.entrySet()) {
        Class<? extends Throwable> key = c.getKey();
        if (key.isAssignableFrom(throwableToSearchIn.getClass())) {
          if (c.getValue() == null || c.getValue().isEmpty()) {
            return true;
          } else {
            String msg = throwableToSearchIn.getMessage();
            for (String val : c.getValue()) {
              if (val.equals(msg)) {
                return true;
              }
            }
          }
        }
      }
      throwableToSearchIn = throwableToSearchIn.getCause();
    }
    return false;
  }
}
