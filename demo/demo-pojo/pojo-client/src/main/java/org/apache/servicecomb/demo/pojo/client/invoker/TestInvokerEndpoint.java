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

package org.apache.servicecomb.demo.pojo.client.invoker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public class TestInvokerEndpoint implements CategorizedTestCase {
  @Override
  public void testRestTransport() throws Exception {
    testInvokerUtilsDiffModelRest();
  }

  @Override
  public void testHighwayTransport() throws Exception {
    testInvokerUtilsDiffModelHighway();
  }

  @Override
  public void testAllTransport() throws Exception {
    testInvokerUtilsDiffModel();
    testInvokerUtilsDiffModelMapArgs();
  }

  private void testInvokerUtilsDiffModelHighway() throws Exception {
    Map<String, Object> args = new HashMap<>();
    ClientModel model = new ClientModel();
    model.setCode(200);
    model.setName("hello");
    args.put("request", model);

    Map result = (Map) InvokerUtils.syncInvoke("pojo", "0+", "highway", "InvokerEndpoint", "model", args);
    TestMgr.check(model.getCode(), result.get("code"));
    TestMgr.check(model.getName(), result.get("name"));

    ClientModel modelResult = InvokerUtils
        .syncInvoke("pojo", "0+", "highway", "InvokerEndpoint", "model", args, ClientModel.class);
    TestMgr.check(model.getCode(), modelResult.getCode());
    TestMgr.check(model.getName(), modelResult.getName());

    CountDownLatch countDownLatch = new CountDownLatch(1);
    InvokerUtils
        .reactiveInvoke("pojo", "0+", "highway", "InvokerEndpoint", "model", args, ClientModel.class, response -> {
          ClientModel reactiveResult = response.getResult();
          TestMgr.check(model.getCode(), reactiveResult.getCode());
          TestMgr.check(model.getName(), reactiveResult.getName());
          System.out.println("done");
          countDownLatch.countDown();
        });
    countDownLatch.await();
  }

  private void testInvokerUtilsDiffModelRest() throws Exception {
    Map<String, Object> args = new HashMap<>();
    ClientModel model = new ClientModel();
    model.setCode(200);
    model.setName("hello");
    args.put("request", model);

    Map result = (Map) InvokerUtils.syncInvoke("pojo", "0+", "rest", "InvokerEndpoint", "model", args);
    TestMgr.check(model.getCode(), result.get("code"));
    TestMgr.check(model.getName(), result.get("name"));

    ClientModel modelResult = InvokerUtils
        .syncInvoke("pojo", "0+", "rest", "InvokerEndpoint", "model", args, ClientModel.class);
    TestMgr.check(model.getCode(), modelResult.getCode());
    TestMgr.check(model.getName(), modelResult.getName());

    CountDownLatch countDownLatch = new CountDownLatch(1);
    InvokerUtils.reactiveInvoke("pojo", "0+", "rest", "InvokerEndpoint", "model", args, ClientModel.class, response -> {
      ClientModel reactiveResult = response.getResult();
      TestMgr.check(model.getCode(), reactiveResult.getCode());
      TestMgr.check(model.getName(), reactiveResult.getName());
      System.out.println("done");
      countDownLatch.countDown();
    });
    countDownLatch.await();
  }

  private void testInvokerUtilsDiffModel() throws Exception {
    Map<String, Object> args = new HashMap<>();
    ClientModel model = new ClientModel();
    model.setCode(200);
    model.setName("hello");
    args.put("request", model);

    Map result = (Map) InvokerUtils.syncInvoke("pojo", "InvokerEndpoint", "model", args);
    TestMgr.check(model.getCode(), result.get("code"));
    TestMgr.check(model.getName(), result.get("name"));

    ClientModel modelResult = InvokerUtils.syncInvoke("pojo", "InvokerEndpoint", "model", args, ClientModel.class);
    TestMgr.check(model.getCode(), modelResult.getCode());
    TestMgr.check(model.getName(), modelResult.getName());

    CountDownLatch countDownLatch = new CountDownLatch(1);
    InvokerUtils.reactiveInvoke("pojo", "InvokerEndpoint", "model", args, ClientModel.class, response -> {
      ClientModel reactiveResult = response.getResult();
      TestMgr.check(model.getCode(), reactiveResult.getCode());
      TestMgr.check(model.getName(), reactiveResult.getName());
      System.out.println("done");
      countDownLatch.countDown();
    });
    countDownLatch.await();
  }

  private void testInvokerUtilsDiffModelMapArgs() throws Exception {
    Map<String, Object> args = new HashMap<>();
    Map model = new HashMap();
    model.put("code", 20);
    model.put("name", "hello");
    args.put("request", model);

    Map result = (Map) InvokerUtils.syncInvoke("pojo", "InvokerEndpoint", "model", args);
    TestMgr.check(model.get("code"), result.get("code"));
    TestMgr.check(model.get("name"), result.get("name"));

    ClientModel modelResult = InvokerUtils.syncInvoke("pojo", "InvokerEndpoint", "model", args, ClientModel.class);
    TestMgr.check(model.get("code"), modelResult.getCode());
    TestMgr.check(model.get("name"), modelResult.getName());

    CountDownLatch countDownLatch = new CountDownLatch(1);
    InvokerUtils.reactiveInvoke("pojo", "InvokerEndpoint", "model", args, ClientModel.class, response -> {
      ClientModel reactiveResult = response.getResult();
      TestMgr.check(model.get("code"), reactiveResult.getCode());
      TestMgr.check(model.get("name"), reactiveResult.getName());
      countDownLatch.countDown();
    });
    countDownLatch.await();
  }
}
