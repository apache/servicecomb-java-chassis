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

package org.apache.servicecomb.config.kie.client;

import java.util.Map;

import org.apache.servicecomb.config.kie.model.KVDoc;
import org.junit.Assert;
import org.junit.Test;

public class TestKieUtil {
  @Test
  public void test_processValueType() {
    KVDoc kvDoc = new KVDoc();
    kvDoc.setKey("hello");
    kvDoc.setValue("world");
    Map<String, Object> result = KieUtil.processValueType(kvDoc);
    Assert.assertEquals("world", result.get("hello"));

    kvDoc.setValueType("text");
    result = KieUtil.processValueType(kvDoc);
    Assert.assertEquals("world", result.get("hello"));

    kvDoc.setValueType("string");
    result = KieUtil.processValueType(kvDoc);
    Assert.assertEquals("world", result.get("hello"));

    kvDoc.setValueType("json");
    result = KieUtil.processValueType(kvDoc);
    Assert.assertEquals("world", result.get("hello"));

    kvDoc.setValueType("yml");
    kvDoc.setValue("hello: world");
    result = KieUtil.processValueType(kvDoc);
    Assert.assertEquals("world", result.get("hello.hello"));

    kvDoc.setValueType("yaml");
    kvDoc.setValue("hello: world");
    result = KieUtil.processValueType(kvDoc);
    Assert.assertEquals("world", result.get("hello.hello"));

    kvDoc.setValueType("properties");
    kvDoc.setValue("hello=world");
    result = KieUtil.processValueType(kvDoc);
    Assert.assertEquals("world", result.get("hello.hello"));
  }
}
