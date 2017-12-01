/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.rest.vertx;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.foundation.common.utils.SPIServiceUtils;
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
    LOGGER.info("set uploads directory to {}.", uploadsDirectory);

    return bodyHandler;
  }
}

