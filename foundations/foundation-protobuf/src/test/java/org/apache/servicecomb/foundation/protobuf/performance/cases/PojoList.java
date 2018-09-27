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

import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot.User;
import org.apache.servicecomb.foundation.protobuf.performance.TestBase;

public class PojoList extends TestBase {
  public PojoList() {
    pojoRoot.setpList(Arrays.asList(
        new org.apache.servicecomb.foundation.protobuf.internal.model.User().name("name1"),
        new org.apache.servicecomb.foundation.protobuf.internal.model.User().name("name2")));

    builder.addPList(User.newBuilder().setName("name1").build())
        .addPList(User.newBuilder().setName("name2").build());
  }
}
