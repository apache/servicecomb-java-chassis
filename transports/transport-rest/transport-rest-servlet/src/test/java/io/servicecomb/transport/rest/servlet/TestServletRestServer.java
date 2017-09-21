/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.rest.servlet;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Holder;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.common.rest.locator.ServicePathManager;
import io.servicecomb.core.CseContext;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.foundation.common.utils.SPIServiceUtils;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.foundation.vertx.http.StandardHttpServletRequestEx;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestServletRestServer {
  ServletRestServer server;

  @Mocked
  HttpServletRequest request;

  @Mocked
  HttpServletResponse response;

  @Mocked
  OperationMeta operationMeta;

  @Test
  public void service() {
    Holder<Boolean> handled = new Holder<>();
    server = new ServletRestServer() {
      @Override
      protected void handleRequest(HttpServletRequestEx requestEx, HttpServletResponseEx responseEx) {
        handled.value = true;
      }
    };

    server.service(request, response);

    Assert.assertTrue(handled.value);
  }

  @Test
  public void findRestOperationCacheTrue(@Mocked MicroserviceMetaManager microserviceMetaManager,
      @Mocked ServicePathManager servicePathManager) {
    Microservice microservice = new Microservice();
    microservice.setServiceName("ms");
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getMicroservice();
        result = microservice;
      }
    };

    HttpServletRequestEx requestEx = new StandardHttpServletRequestEx(request);
    server = new ServletRestServer() {
      @Override
      protected boolean collectCacheRequest(OperationMeta operationMeta) {
        return true;
      }
    };

    CseContext.getInstance().setMicroserviceMetaManager(microserviceMetaManager);

    server.findRestOperation(requestEx);
    Assert.assertTrue(Deencapsulation.getField(requestEx, "cacheRequest"));

    CseContext.getInstance().setMicroserviceMetaManager(null);
  }

  @Test
  public void collectCacheRequestCacheTrue(@Mocked HttpServerFilter f1) {
    new Expectations(SPIServiceUtils.class) {
      {
        f1.needCacheRequest(operationMeta);
        result = true;
        SPIServiceUtils.getSortedService(HttpServerFilter.class);
        result = Arrays.asList(f1);
      }
    };

    server = new ServletRestServer();
    Assert.assertTrue(server.collectCacheRequest(operationMeta));
  }

  @Test
  public void collectCacheRequestCacheFalse(@Mocked HttpServerFilter f1) {
    new Expectations(SPIServiceUtils.class) {
      {
        f1.needCacheRequest(operationMeta);
        result = false;
        SPIServiceUtils.getSortedService(HttpServerFilter.class);
        result = Arrays.asList(f1);
      }
    };

    server = new ServletRestServer();
    Assert.assertFalse(server.collectCacheRequest(operationMeta));
  }

}
