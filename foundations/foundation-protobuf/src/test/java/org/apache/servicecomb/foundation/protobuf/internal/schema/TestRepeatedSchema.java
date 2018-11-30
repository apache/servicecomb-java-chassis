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
package org.apache.servicecomb.foundation.protobuf.internal.schema;

import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.foundation.protobuf.internal.TestSchemaBase;
import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot;
import org.junit.Assert;
import org.junit.Test;

public class TestRepeatedSchema extends TestSchemaBase {
  public static class RootWithArray {
    public String[] sList;
  }

  @Test
  public void sList() throws Throwable {
    List<String> sList = Arrays.asList("v1", "v2");
    builder.addAllSList(sList);
    check();

    RootWithArray rootWithArray = new RootWithArray();
    rootWithArray.sList = (String[]) sList.toArray();
    Assert.assertArrayEquals(protobufBytes, rootSerializer.serialize(rootWithArray));
  }

  @Test
  public void pList() throws Throwable {
    builder.addPList(ProtobufRoot.User.newBuilder().setName("name1").build());
    builder.addPList(ProtobufRoot.User.newBuilder().setName("name2").build());

    check();
  }
}
