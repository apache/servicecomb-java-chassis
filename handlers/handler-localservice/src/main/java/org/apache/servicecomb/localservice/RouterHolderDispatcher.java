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

package org.apache.servicecomb.localservice;

import org.apache.servicecomb.transport.rest.vertx.VertxHttpDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.ext.web.Router;

/**
 * A VertxHttpDispatcher that captures the Router reference from RestServerVerticle.
 * This allows LocalServiceBootListener to reuse the same Router instance
 * instead of creating a new one, enabling proper handler chain sharing.
 *
 * <p>This dispatcher runs first (order = Integer.MIN_VALUE) to capture the Router
 * before any other dispatcher processes it.
 */
public class RouterHolderDispatcher implements VertxHttpDispatcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(RouterHolderDispatcher.class);

  private static volatile Router capturedRouter;

  @Override
  public int getOrder() {
    return Integer.MIN_VALUE;
  }

  @Override
  public boolean enabled() {
    return LocalServiceConfig.isEnabled();
  }

  @Override
  public void init(Router router) {
    capturedRouter = router;
    LOGGER.debug("Captured Router reference for local service listener");
  }

  /**
   * Get the captured Router instance.
   *
   * @return the Router captured from RestServerVerticle, or null if not captured
   */
  public static Router getCapturedRouter() {
    return capturedRouter;
  }

  /**
   * Reset the captured Router. This is useful for testing or when the application is shutting down.
   */
  public static void reset() {
    capturedRouter = null;
    LOGGER.debug("Reset captured Router reference");
  }
}
