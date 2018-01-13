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

package org.apache.servicecomb.transport.rest.vertx;

import static io.vertx.ext.web.handler.BodyHandler.DEFAULT_BODY_LIMIT;

import java.util.List;

import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

import io.vertx.ext.web.handler.BodyHandler;

public abstract class AbstractVertxHttpDispatcher implements VertxHttpDispatcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractVertxHttpDispatcher.class);

  protected List<HttpServerFilter> httpServerFilters = SPIServiceUtils.getSortedService(HttpServerFilter.class);

  public AbstractVertxHttpDispatcher() {
    for (HttpServerFilter filter : httpServerFilters) {
      LOGGER.info("Found HttpServerFilter: {}.", filter.getClass().getName());
    }
  }

  protected BodyHandler createBodyHandler() {
    RestBodyHandler bodyHandler = new RestBodyHandler();

    String uploadsDirectory =
        DynamicPropertyFactory.getInstance().getStringProperty("cse.uploads.directory", null).get();
    bodyHandler.setUploadsDirectory(uploadsDirectory);
    bodyHandler.setDeleteUploadedFilesOnEnd(true);
    bodyHandler.setBodyLimit(
        DynamicPropertyFactory.getInstance().getLongProperty("cse.uploads.maxSize", DEFAULT_BODY_LIMIT).get());
    LOGGER.info("set uploads directory to {}.", uploadsDirectory);

    return bodyHandler;
  }
}

