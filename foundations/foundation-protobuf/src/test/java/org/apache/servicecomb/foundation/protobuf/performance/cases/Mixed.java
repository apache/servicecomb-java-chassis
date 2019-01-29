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
package org.apache.servicecomb.foundation.protobuf.performance.cases;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot;
import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot.User;
import org.apache.servicecomb.foundation.protobuf.performance.TestBase;
import org.apache.servicecomb.foundation.test.scaffolding.model.Color;

public class Mixed extends TestBase {
  public Mixed() {
    pojoRoot.setInt32(10000);
    pojoRoot.setInt64(20000L);
    pojoRoot.setUint32(30000);
    pojoRoot.setUint64(40000L);
    pojoRoot.setSint32(50000);
    pojoRoot.setSint64(60000L);
    pojoRoot.setFixed32(70000);
    pojoRoot.setFixed64(80000L);
    pojoRoot.setSfixed32(90000);
    pojoRoot.setSfixed64(100000L);
    pojoRoot.setFloatValue((float) 10000);
    pojoRoot.setDoubleValue(20000.0);
    pojoRoot.setBool(true);

    pojoRoot.setObjInt32(10000);
    pojoRoot.setObjInt64(20000L);
    pojoRoot.setObjUint32(30000);
    pojoRoot.setObjUint64(40000L);
    pojoRoot.setObjSint32(50000);
    pojoRoot.setObjSint64(60000L);
    pojoRoot.setObjFixed32(70000);
    pojoRoot.setObjFixed64(80000L);
    pojoRoot.setObjSfixed32(90000);
    pojoRoot.setObjSfixed64(100000L);
    pojoRoot.setObjFloatValue((float) 10000);
    pojoRoot.setObjDoubleValue(20000.0);
    pojoRoot.setObjBool(true);

    pojoRoot.setString("string value");
    pojoRoot.setColor(Color.BLUE);
    pojoRoot.setUser(new org.apache.servicecomb.foundation.protobuf.internal.model.User("name1"));

    pojoRoot.setSsMap(new LinkedHashMap<>());
    pojoRoot.getSsMap().put("k1", "v1");
    pojoRoot.getSsMap().put("k2", "v2");
    pojoRoot.setSpMap(new LinkedHashMap<>());
    pojoRoot.getSpMap().put("u1", new org.apache.servicecomb.foundation.protobuf.internal.model.User().name("name1"));
    pojoRoot.getSpMap().put("u2", new org.apache.servicecomb.foundation.protobuf.internal.model.User().name("name2"));

    pojoRoot.setInt32sPacked(Arrays.asList(10000, 20000, 30000));
    pojoRoot.setInt64sPacked(Arrays.asList(10000L, 20000L, 30000L));
    pojoRoot.setUint32sPacked(Arrays.asList(10000, 20000, 30000));
    pojoRoot.setUint64sPacked(Arrays.asList(10000L, 20000L, 30000L));
    pojoRoot.setSint32sPacked(Arrays.asList(10000, 20000, 30000));
    pojoRoot.setSint64sPacked(Arrays.asList(10000L, 20000L, 30000L));
    pojoRoot.setFixed32sPacked(Arrays.asList(10000, 20000, 30000));
    pojoRoot.setFixed64sPacked(Arrays.asList(10000L, 20000L, 30000L));
    pojoRoot.setSfixed32sPacked(Arrays.asList(10000, 20000, 30000));
    pojoRoot.setSfixed64sPacked(Arrays.asList(10000L, 20000L, 30000L));
    pojoRoot.setFloatsPacked(Arrays.asList((float) 10000, (float) 20000, (float) 30000));
    pojoRoot.setDoublesPacked(Arrays.asList(10000.0, 20000.0, 30000.0));
    pojoRoot.setBoolsPacked(Arrays.asList(true, false));
    pojoRoot.setColorsPacked(Arrays.asList(Color.RED, Color.BLUE));

    pojoRoot.setStrings(Arrays.asList("string value1", "string value2"));
    pojoRoot.setUsers(Arrays.asList(
        new org.apache.servicecomb.foundation.protobuf.internal.model.User().name("name1"),
        new org.apache.servicecomb.foundation.protobuf.internal.model.User().name("name2"),
        new org.apache.servicecomb.foundation.protobuf.internal.model.User().name("name3"),
        new org.apache.servicecomb.foundation.protobuf.internal.model.User().name("name4")));

    builder.setInt32(10000)
        .setInt64(20000L)
        .setUint32(30000)
        .setUint64(40000L)
        .setSint32(50000)
        .setSint64(60000L)
        .setFixed32(70000)
        .setFixed64(80000L)
        .setSfixed32(90000)
        .setSfixed64(100000L)
        .setFloatValue((float) 10000)
        .setDoubleValue(20000.0)
        .setBool(true)

        .setObjInt32(10000)
        .setObjInt64(20000L)
        .setObjUint32(30000)
        .setObjUint64(40000L)
        .setObjSint32(50000)
        .setObjSint64(60000L)
        .setObjFixed32(70000)
        .setObjFixed64(80000L)
        .setObjSfixed32(90000)
        .setObjSfixed64(100000L)
        .setObjFloatValue((float) 10000)
        .setObjDoubleValue(20000.0)
        .setObjBool(true)

        .setString("string value")
        .setColorValue(2)
        .setUser(User.newBuilder().setName("name1").build())

        .putSsMap("k1", "v1")
        .putSsMap("k2", "v2")
        .putSpMap("u1", User.newBuilder().setName("name1").build())
        .putSpMap("u2", User.newBuilder().setName("name2").build())

        .addAllInt32SPacked(Arrays.asList(10000, 20000, 30000))
        .addAllInt64SPacked(Arrays.asList(10000L, 20000L, 30000L))
        .addAllUint32SPacked(Arrays.asList(10000, 20000, 30000))
        .addAllUint64SPacked(Arrays.asList(10000L, 20000L, 30000L))
        .addAllSint32SPacked(Arrays.asList(10000, 20000, 30000))
        .addAllSint64SPacked(Arrays.asList(10000L, 20000L, 30000L))
        .addAllFixed32SPacked(Arrays.asList(10000, 20000, 30000))
        .addAllFixed64SPacked(Arrays.asList(10000L, 20000L, 30000L))
        .addAllSfixed32SPacked(Arrays.asList(10000, 20000, 30000))
        .addAllSfixed64SPacked(Arrays.asList(10000L, 20000L, 30000L))
        .addAllFloatsPacked(Arrays.asList((float) 10000, (float) 20000, (float) 30000))
        .addAllDoublesPacked(Arrays.asList(10000.0, 20000.0, 30000.0))
        .addAllBoolsPacked(Arrays.asList(true, false))
        .addAllColorsPacked(Arrays.asList(ProtobufRoot.Color.RED, ProtobufRoot.Color.BLUE))

        .addStrings("string value1")
        .addStrings("string value2")
        .addUsers(User.newBuilder().setName("name1").build())
        .addUsers(User.newBuilder().setName("name2").build())
        .addUsers(User.newBuilder().setName("name3").build())
        .addUsers(User.newBuilder().setName("name4").build());
  }
}
