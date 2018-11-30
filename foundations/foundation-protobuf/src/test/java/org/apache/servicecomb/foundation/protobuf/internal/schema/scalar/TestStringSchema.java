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

import org.apache.servicecomb.foundation.protobuf.internal.TestSchemaBase;
import org.apache.servicecomb.foundation.protobuf.internal.model.User;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class TestStringSchema extends TestSchemaBase {
  public TestStringSchema() {
    initField("string");
  }

  @Test
  public void normal() throws Throwable {
    String value = "abc";
    builder.setString(value);
    check();

    // string[]
    Assert.assertArrayEquals(protobufBytes, serFieldSchema.writeTo(new String[] {value}));

    // string
    Assert.assertArrayEquals(protobufBytes, serFieldSchema.writeTo(value));
  }

  @Test
  public void nullOrEmpty() throws Throwable {
    // null
    Assert.assertEquals(0, serFieldSchema.writeTo(null).length);

    // empty string[]
    Assert.assertEquals(0, serFieldSchema.writeTo(new String[] {}).length);
  }

  @Test
  public void type_invalid() throws Throwable {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers
        .is("not support serialize from org.apache.servicecomb.foundation.protobuf.internal.model.User to proto string, field=org.apache.servicecomb.foundation.protobuf.internal.model.Root:string"));

    serFieldSchema.writeTo(new User());
  }
}
