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
package org.apache.servicecomb.foundation.vertx;

import org.apache.servicecomb.foundation.vertx.metrics.DefaultVertxMetricsFactory;
import org.apache.servicecomb.foundation.vertx.metrics.MetricsOptionsEx;
import org.springframework.core.env.Environment;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.shareddata.Shareable;

public class SharedVertxFactory {
  static class SharedVertxInfo implements Shareable {
    public VertxOptions vertxOptions = new VertxOptions();

    public DefaultVertxMetricsFactory metricsFactory = new DefaultVertxMetricsFactory();

    public MetricsOptionsEx metricsOptionsEx = (MetricsOptionsEx) metricsFactory.newOptions();

    public SharedVertxInfo(Environment environment) {
      vertxOptions.setMetricsOptions(metricsOptionsEx);
      vertxOptions.setEventLoopPoolSize(readEventLoopPoolSize(environment, "servicecomb.transport.eventloop.size"));
    }

    private static int readEventLoopPoolSize(Environment environment, String key) {
      int count = environment.getProperty(key, int.class, -1);
      if (count > 0) {
        return count;
      }
      return VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE;
    }
  }

  private static final String LOCAL_MAP_NAME = "scb";

  private static final String INFO = "transport-vertx-info";

  public static DefaultVertxMetricsFactory getMetricsFactory(Environment environment) {
    SharedVertxInfo info = (SharedVertxInfo) getSharedVertx(environment).sharedData().getLocalMap(LOCAL_MAP_NAME)
        .get(INFO);
    return info.metricsFactory;
  }

  public static Vertx getSharedVertx(Environment environment) {
    return VertxUtils.getVertxMap().computeIfAbsent("transport",
        key -> createSharedVertx(environment, key));
  }

  private static Vertx createSharedVertx(Environment environment, String name) {
    SharedVertxInfo info = new SharedVertxInfo(environment);

    Vertx vertx = VertxUtils.init(name, info.vertxOptions);
    info.metricsFactory.setVertx(vertx, info.vertxOptions);
    vertx.sharedData().getLocalMap(LOCAL_MAP_NAME).put(INFO, info);

    return vertx;
  }
}
