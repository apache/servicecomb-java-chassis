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

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import io.vertx.ext.web.Router;
import mockit.Deencapsulation;

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

  Environment environment = Mockito.mock(Environment.class);

  @Before
  public void setUp() {
    LegacyPropertyFactory.setEnvironment(environment);
    Mockito.when(environment.getProperty(
            RestConst.UPLOAD_MAX_SIZE, long.class, -1L))
        .thenReturn(-1L);
    Mockito.when(environment.getProperty(RestConst.UPLOAD_MAX_FILE_SIZE, long.class, -1L))
        .thenReturn(-1L);
    Mockito.when(environment.getProperty(RestConst.UPLOAD_FILE_SIZE_THRESHOLD, int.class, 0))
        .thenReturn(0);
  }

  @Test
  public void createBodyHandlerUploadDefault() {
    Mockito.when(environment.getProperty(
            "servicecomb.uploads.directory", String.class, RestConst.UPLOAD_DEFAULT_DIR))
        .thenReturn(RestConst.UPLOAD_DEFAULT_DIR);

    AbstractVertxHttpDispatcher dispatcher = new AbstractVertxHttpDispatcherForTest();
    RestBodyHandler bodyHandler = (RestBodyHandler) dispatcher.createBodyHandler();

    Assertions.assertTrue(bodyHandler.isDeleteUploadedFilesOnEnd());
    Assertions.assertEquals(RestConst.UPLOAD_DEFAULT_DIR, Deencapsulation.getField(bodyHandler, "uploadsDir"));
  }

  @Test
  public void createBodyHandlerUploadNormal() {
    Mockito.when(environment.getProperty(
            "servicecomb.uploads.directory", String.class, RestConst.UPLOAD_DEFAULT_DIR))
        .thenReturn("/path");

    AbstractVertxHttpDispatcher dispatcher = new AbstractVertxHttpDispatcherForTest();
    RestBodyHandler bodyHandler = (RestBodyHandler) dispatcher.createBodyHandler();

    Assertions.assertTrue(bodyHandler.isDeleteUploadedFilesOnEnd());
    Assertions.assertEquals("/path", Deencapsulation.getField(bodyHandler, "uploadsDir"));
  }
}
