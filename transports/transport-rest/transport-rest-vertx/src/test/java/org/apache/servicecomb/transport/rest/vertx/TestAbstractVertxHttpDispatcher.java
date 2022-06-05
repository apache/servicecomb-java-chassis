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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runners.MethodSorters;

import com.netflix.config.DynamicPropertyFactory;

import io.vertx.ext.web.Router;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestAbstractVertxHttpDispatcher {
  static class AbstractVertxHttpDispatcherForTest extends AbstractVertxHttpDispatcher {
    @Override
    public int getOrder() {
      return 0;
    }

    @Override
    public void init(Router router) {
    }
  }

  Configuration config = (Configuration) DynamicPropertyFactory.getBackingConfigurationSource();

  @BeforeClass
  public static void setup() {
    ArchaiusUtils.resetConfig();
  }


  @AfterClass
  public static void teardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void construct(@Mocked HttpServerFilter filter) {
    List<HttpServerFilter> filters = Arrays.asList(filter);
    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getSortedService(HttpServerFilter.class);
        result = filters;
      }
    };

    AbstractVertxHttpDispatcher dispatcher = new AbstractVertxHttpDispatcherForTest();
    Assertions.assertSame(filters, dispatcher.httpServerFilters);
  }

  @Test
  public void createBodyHandlerUploadDefault() {
    AbstractVertxHttpDispatcher dispatcher = new AbstractVertxHttpDispatcherForTest();
    RestBodyHandler bodyHandler = (RestBodyHandler) dispatcher.createBodyHandler();

    Assertions.assertTrue(bodyHandler.isDeleteUploadedFilesOnEnd());
    Assertions.assertEquals(RestConst.UPLOAD_DEFAULT_DIR, Deencapsulation.getField(bodyHandler, "uploadsDir"));
  }

  @Test
  public void createBodyHandlerUploadNormal() {
    config.setProperty("servicecomb.uploads.directory", "/path");

    AbstractVertxHttpDispatcher dispatcher = new AbstractVertxHttpDispatcherForTest();
    RestBodyHandler bodyHandler = (RestBodyHandler) dispatcher.createBodyHandler();

    Assertions.assertTrue(bodyHandler.isDeleteUploadedFilesOnEnd());
    Assertions.assertEquals("/path", Deencapsulation.getField(bodyHandler, "uploadsDir"));
  }
}
