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

import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.server.GenericsModel;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

/*
This is the provider definition:

<code>
  @GetMapping(path = "/diffNames")
  @ApiOperation(value = "differentName", nickname = "differentName")
  public int diffNames(@RequestParam("x") int a, @RequestParam("y") int b)
</code>

and the swagger is:

<code>
  paths:
    /diffNames:
      get:
        summary: "differentName"
        operationId: "differentName"
        parameters:
        - name: "x"
          in: "query"
          required: true
          type: "integer"
          format: "int32"
        - name: "y"
          in: "query"
          required: true
          type: "integer"
          format: "int32"
        responses:
          "200":
            description: "response of 200"
            schema:
              type: "integer"
              format: "int32"
</code>

In consumer, you can define any prototype that matches generated swagger of provider.
 */

interface DiffNames {
  int differentName(int x, int y);
}

interface DiffNames2 {
  int differentName(int y, int x);
}

interface Generics {
  List<List<String>> genericParams(int code, List<List<String>> names);
}

interface GenericsModelInf {
  GenericsModel genericParamsModel(int code, GenericsModel model);
}

interface ObjectInf {
  GenericsModel obj(GenericsModel obj);
}

@Component
public class TestWeakPojo implements CategorizedTestCase {
  @RpcReference(microserviceName = "pojo", schemaId = "WeakPojo")
  private DiffNames diffNames;

  @RpcReference(microserviceName = "pojo", schemaId = "WeakPojo")
  private DiffNames2 diffNames2;

  @RpcReference(microserviceName = "pojo", schemaId = "WeakPojo")
  private Generics generics;

  @RpcReference(microserviceName = "pojo", schemaId = "WeakPojo")
  private GenericsModelInf genericsModelInf;

  @RpcReference(microserviceName = "pojo", schemaId = "WeakPojo")
  private ObjectInf objectInf;

  @Override
  public void testAllTransport() throws Exception {
    testDiffName();

    testGenerics();

    testGenericsModel();

    testObject();
  }

  private void testObject() throws JsonProcessingException {
    GenericsModel model = new GenericsModel();
    model.setName("model");
    List<List<String>> namesList = new ArrayList<>();
    List<String> names = new ArrayList<>();
    names.add("hello");
    namesList.add(names);
    model.setNameList(namesList);
    List<List<List<Object>>> objectLists = new ArrayList<>();
    List<List<Object>> objectList = new ArrayList<>();
    List<Object> objects = new ArrayList<>();
    objects.add("object");
    objectList.add(objects);
    objectLists.add(objectList);
    model.setObjectLists(objectLists);
    GenericsModel result = objectInf.obj(model);
    TestMgr.check(JsonUtils.writeValueAsString(model), JsonUtils.writeValueAsString(result));
  }

  private void testGenericsModel() throws JsonProcessingException {
    GenericsModel model = new GenericsModel();
    model.setName("model");
    List<List<String>> namesList = new ArrayList<>();
    List<String> names = new ArrayList<>();
    names.add("hello");
    namesList.add(names);
    model.setNameList(namesList);
    List<List<List<Object>>> objectLists = new ArrayList<>();
    List<List<Object>> objectList = new ArrayList<>();
    List<Object> objects = new ArrayList<>();
    objects.add("object");
    objectList.add(objects);
    objectLists.add(objectList);
    model.setObjectLists(objectLists);
    GenericsModel result = genericsModelInf.genericParamsModel(100, model);
    TestMgr.check(JsonUtils.writeValueAsString(model), JsonUtils.writeValueAsString(result));
  }

  private void testGenerics() {
    List<List<String>> namesList = new ArrayList<>();
    List<String> names = new ArrayList<>();
    names.add("hello");
    namesList.add(names);
    List<List<String>> nameListResult = generics.genericParams(100, namesList);
    TestMgr.check(1, nameListResult.size());
    TestMgr.check(1, nameListResult.get(0).size());
    TestMgr.check("hello", nameListResult.get(0).get(0));
  }

  private void testDiffName() {
    TestMgr.check(7, diffNames.differentName(2, 3));
    TestMgr.check(8, diffNames2.differentName(2, 3));
    Map<String, Object> args = new HashMap<>();
    args.put("x", 2);
    args.put("y", 3);
    Map<String, Object> swaggerArgs = new HashMap<>();
    swaggerArgs.put("differentNameBody", args);
    TestMgr.check(7, InvokerUtils.syncInvoke("pojo", "WeakPojo", "differentName", swaggerArgs));
  }
}
