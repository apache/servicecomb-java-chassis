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

package org.apache.servicecomb.demo.pojo.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.server.AbstractModel;
import org.apache.servicecomb.demo.server.DefaultAbstractModel;
import org.apache.servicecomb.demo.server.NotRecommendedServiceInf;
import org.apache.servicecomb.demo.server.SecondAbstractModel;
import org.apache.servicecomb.demo.server.WrappedAbstractModel;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class TestNotRecommendedService implements CategorizedTestCase {
  @RpcReference(microserviceName = "pojo", schemaId = "NotRecommendedService")
  private NotRecommendedServiceInf pojo;

  @Override
  public void testRestTransport() throws Exception {
    testLongMap();
    testAbstractModel();
    testListAbstractModel();
    testMapAbstractModel();
    testWrappedModel();
  }

  private void testWrappedModel() {
    Map<Long, AbstractModel> data = new HashMap<>();
    AbstractModel model = new DefaultAbstractModel();
    model.setName("hello");
    data.put(100L, model);

    List<AbstractModel> data2 = new ArrayList<>();

    AbstractModel model2 = new DefaultAbstractModel();
    model2.setName("hello");
    data2.add(model2);

    WrappedAbstractModel input = new WrappedAbstractModel();
    input.setMapModel(data);
    input.setListModel(data2);
    AbstractModel secondModel = new SecondAbstractModel();
    secondModel.setName("second");
    input.setModel(secondModel);
    input.setName("wrapped");

    WrappedAbstractModel result = pojo.wrappedAbstractModel(input);

    TestMgr.check(1, result.getMapModel().size());
    TestMgr.check("hello", result.getMapModel().get(result.getMapModel().keySet().iterator().next()).getName());

    TestMgr.check(1, result.getListModel().size());
    TestMgr.check("hello", result.getListModel().get(0).getName());

    TestMgr.check("second", result.getModel().getName());
    TestMgr.check(true, result.getModel() instanceof SecondAbstractModel);
    TestMgr.check("wrapped", result.getName());
  }

  private void testMapAbstractModel() {
    Map<Long, AbstractModel> data = new HashMap<>();
    AbstractModel model = new DefaultAbstractModel();
    model.setName("hello");
    data.put(100L, model);
    Map<Long, AbstractModel> result = pojo.mapAbstractModel(data);
    TestMgr.check(1, result.size());
    TestMgr.check("hello", result.get(result.keySet().iterator().next()).getName());
  }

  private void testListAbstractModel() {
    List<AbstractModel> data = new ArrayList<>();
    AbstractModel model = new DefaultAbstractModel();
    model.setName("hello");
    data.add(model);
    List<AbstractModel> result = pojo.listAbstractModel(data);
    TestMgr.check(1, result.size());
    TestMgr.check("hello", result.get(0).getName());
  }

  private void testAbstractModel() {
    AbstractModel model = new DefaultAbstractModel();
    model.setName("hello");

    AbstractModel result = pojo.abstractModel(model);
    TestMgr.check("hello", result.getName());
  }

  private void testLongMap() {
    Map<Long, Long> data = new HashMap<>();
    data.put(100L, 200L);
    Map<Long, Long> result = pojo.longMap(data);
    TestMgr.check(1, result.size());
    TestMgr.check(200L, result.get(100L));
  }
}
