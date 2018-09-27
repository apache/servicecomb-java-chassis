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

import java.lang.reflect.Method;
import java.util.Locale;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.foundation.protobuf.internal.TestSchemaBase;
import org.apache.servicecomb.foundation.protobuf.internal.model.User;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.protostuff.compiler.model.Type;

@Ignore
public abstract class TestNumberBaseSchema extends TestSchemaBase {
  protected String fieldName;

  protected Object minValue;

  protected Object maxValue;

  public TestNumberBaseSchema() {
    init();
  }

  protected void init() {
    initField(fieldName);
  }

  @Test
  public void normal() throws Throwable {
    // normal
    Object value = doTestNormal();

    // null
    Assert.assertEquals(0, serFieldSchema.writeTo(null).length);

    // empty string[]
    Assert.assertEquals(0, serFieldSchema.writeTo(new String[] {}).length);

    // string[]
    Assert.assertArrayEquals(protobufBytes, serFieldSchema.writeTo(new String[] {String.valueOf(value)}));

    // string
    Assert.assertArrayEquals(protobufBytes, serFieldSchema.writeTo(String.valueOf(value)));
  }

  protected Object doTestNormal() throws Throwable {
    String setName = "set" + fieldName.substring(0, 1).toUpperCase(Locale.US) + fieldName.substring(1);
    Method builderSetter = ReflectUtils.findMethod(builder.getClass(), setName);

    builderSetter.invoke(builder, minValue);
    check();

    builderSetter.invoke(builder, maxValue);
    check();

    return maxValue;
  }

  @Test
  public void strings_invalid() throws Throwable {
    expectedException.expect(NumberFormatException.class);
    expectedException.expectMessage(Matchers.is("For input string: \"a\""));

    serFieldSchema.writeTo(new String[] {"a"});
  }

  @Test
  public void string_invalid() throws Throwable {
    expectedException.expect(NumberFormatException.class);
    expectedException.expectMessage(Matchers.is("For input string: \"a\""));

    serFieldSchema.writeTo("a");
  }

  @Test
  public void type_invalid() throws Throwable {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers
        .is(String.format("not support serialize from %s to proto %s, field=%s:%s",
            User.class.getName(),
            serFieldSchema.getProtoField().getTypeName(),
            ((Type) serFieldSchema.getProtoField().getParent()).getCanonicalName(),
            serFieldSchema.getProtoField().getName())));

    serFieldSchema.writeTo(new User());
  }
}
