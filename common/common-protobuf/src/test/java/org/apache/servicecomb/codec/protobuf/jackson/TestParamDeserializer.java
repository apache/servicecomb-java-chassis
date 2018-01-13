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

package org.apache.servicecomb.codec.protobuf.jackson;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.codec.protobuf.codec.AbstractFieldCodec.ReaderHelpData;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestParamDeserializer {

  private ParamDeserializer paramDeserializer = null;

  private Map<String, ReaderHelpData> readerHelpDataMap = new HashMap<>();

  @Before
  public void setUp() throws Exception {
    paramDeserializer = new ParamDeserializer(readerHelpDataMap);
  }

  @After
  public void tearDown() throws Exception {
    paramDeserializer = null;
  }

  @Test
  public void testCreateResult() {
    Object object = paramDeserializer.createResult();
    Assert.assertNotNull(object);
    // object is created but no values inside to assert
  }

  @Test
  public void testUpdateResult() {
    String[] stringArray = new String[1];
    stringArray[0] = "abc";
    Object[] object = new Object[1];
    Object paramObject = paramDeserializer.updateResult(object, stringArray, new ReaderHelpData());
    Assert.assertNotNull(paramObject);
    Assert.assertEquals(paramObject, object);
  }
}
