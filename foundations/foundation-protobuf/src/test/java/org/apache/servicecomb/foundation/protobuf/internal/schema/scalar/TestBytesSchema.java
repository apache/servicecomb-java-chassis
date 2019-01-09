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
package org.apache.servicecomb.foundation.protobuf.internal.schema.scalar;

import java.util.HashMap;

import org.apache.servicecomb.foundation.protobuf.internal.TestSchemaBase;
import org.apache.servicecomb.foundation.protobuf.internal.model.User;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.protobuf.ByteString;

public class TestBytesSchema extends TestSchemaBase {
  public TestBytesSchema() {
    initField("bytes");
  }

  @Test
  public void normal() throws Throwable {
    byte[] value = "abc".getBytes();
    builder.setBytes(ByteString.copyFrom(value));
    check();
  }

  @Test
  public void type_invalid() throws Throwable {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers
        .is("not support serialize from org.apache.servicecomb.foundation.protobuf.internal.model.User to proto bytes, field=org.apache.servicecomb.foundation.protobuf.internal.model.Root:bytes"));

    scbMap = new HashMap<>();
    scbMap.put("bytes", new User());
    rootSerializer.serialize(scbMap);
  }
}
