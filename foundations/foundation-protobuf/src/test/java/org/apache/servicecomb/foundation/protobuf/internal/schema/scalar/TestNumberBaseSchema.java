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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.foundation.protobuf.internal.TestSchemaBase;
import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot;
import org.apache.servicecomb.foundation.test.scaffolding.model.User;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Type;

@Ignore
public abstract class TestNumberBaseSchema extends TestSchemaBase {
  protected Object minValue;

  protected Object maxValue;

  @Test
  public void normal() throws Throwable {
    byte[] primitiveBytes = doTestPojoNormal(primitiveFieldName);
    byte[] bytes = doTestPojoNormal(fieldName);

    doTestMapNormal(primitiveFieldName, maxValue, primitiveBytes);
    doTestMapNormal(fieldName, maxValue, bytes);
  }

  private void doTestMapNormal(String field, Object value, byte[] expectBytes) throws IOException {
    // null
    scbMap = new HashMap<>();
    scbMap.put(field, null);
    Assert.assertEquals(0, rootSerializer.serialize(scbMap).length);

    // empty string[]
    scbMap.put(field, new String[] {});
    Assert.assertEquals(0, rootSerializer.serialize(scbMap).length);

    // string[]
    scbMap.put(field, new String[] {String.valueOf(value)});
    Assert.assertArrayEquals(expectBytes, rootSerializer.serialize(scbMap));

    // string
    scbMap.put(field, String.valueOf(value));
    Assert.assertArrayEquals(expectBytes, rootSerializer.serialize(scbMap));
  }

  private byte[] doTestPojoNormal(String name) throws Throwable {
    builder = ProtobufRoot.Root.newBuilder();
    String setName = "set" + name.substring(0, 1).toUpperCase(Locale.US) + name.substring(1);
    Method builderSetter = ReflectUtils.findMethod(builder.getClass(), setName);

    builderSetter.invoke(builder, minValue);
    check();

    builderSetter.invoke(builder, maxValue);
    check();

    return protobufBytes;
  }

  @Test
  public void primitiveStrings_invalid() throws Throwable {
    strings_invalid(primitiveProtoField);
  }

  @Test
  public void strings_invalid() throws Throwable {
    strings_invalid(protoField);
  }

  private void strings_invalid(Field field) throws IOException {
    expectedException.expect(NumberFormatException.class);
    expectedException.expectMessage(Matchers.is("For input string: \"a\""));

    scbMap = new HashMap<>();
    scbMap.put(field.getName(), new String[] {"a"});
    rootSerializer.serialize(scbMap);
  }

  @Test
  public void primitiveString_invalid() throws Throwable {
    string_invalid(primitiveProtoField);
  }

  @Test
  public void string_invalid() throws Throwable {
    string_invalid(protoField);
  }

  private void string_invalid(Field field) throws IOException {
    expectedException.expect(NumberFormatException.class);
    expectedException.expectMessage(Matchers.is("For input string: \"a\""));

    scbMap = new HashMap<>();
    scbMap.put(field.getName(), "a");
    rootSerializer.serialize(scbMap);
  }

  @Test
  public void primitiveType_invalid() throws Throwable {
    type_invalid(primitiveProtoField);
  }

  @Test
  public void type_invalid() throws Throwable {
    type_invalid(protoField);
  }

  private void type_invalid(Field field) throws IOException {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers
        .is(String.format("not support serialize from %s to proto %s, field=%s:%s",
            User.class.getName(),
            field.getTypeName(),
            ((Type) field.getParent()).getCanonicalName(),
            field.getName())));

    scbMap = new HashMap<>();
    scbMap.put(field.getName(), new User());
    rootSerializer.serialize(scbMap);
  }
}
