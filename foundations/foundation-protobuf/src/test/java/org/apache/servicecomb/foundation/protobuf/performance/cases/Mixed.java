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
import java.util.HashMap;

import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot.User;
import org.apache.servicecomb.foundation.protobuf.performance.TestBase;
import org.apache.servicecomb.foundation.test.scaffolding.model.Color;

public class Mixed extends TestBase {
  public Mixed() {
    pojoRoot.setInt32(1);
    pojoRoot.setInt64(1L);
    pojoRoot.setString("string value");
    pojoRoot.setColor(Color.BLUE);
    pojoRoot.setsList(Arrays.asList("string value1", "string value2"));
    pojoRoot.setpList(Arrays.asList(
        new org.apache.servicecomb.foundation.protobuf.internal.model.User().name("name1"),
        new org.apache.servicecomb.foundation.protobuf.internal.model.User().name("name2")));
    pojoRoot.setSsMap(new HashMap<>());
    pojoRoot.getSsMap().put("k1", "v1");
    pojoRoot.getSsMap().put("k2", "v2");
    pojoRoot.setSpMap(new HashMap<>());
    pojoRoot.getSpMap().put("u1", new org.apache.servicecomb.foundation.protobuf.internal.model.User().name("name1"));
    pojoRoot.getSpMap().put("u2", new org.apache.servicecomb.foundation.protobuf.internal.model.User().name("name2"));

    builder.setInt32(1)
        .setInt64(1L)
        .setString("string value")
        .setColorValue(2)
        .addSList("string value1")
        .addSList("string value2")
        .addPList(User.newBuilder().setName("name1").build())
        .addPList(User.newBuilder().setName("name2").build())
        .putSsMap("k1", "v1")
        .putSsMap("k2", "v2")
        .putSpMap("u1", User.newBuilder().setName("name1").build())
        .putSpMap("u2", User.newBuilder().setName("name2").build());
  }
}
