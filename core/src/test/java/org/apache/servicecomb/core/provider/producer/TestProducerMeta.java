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
package org.apache.servicecomb.core.provider.producer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestProducerMeta {
  @Test
  public void test1() {
    Object instance = new Object();
    ProducerMeta meta = new ProducerMeta("id", instance);
    Assertions.assertEquals("id", meta.getSchemaId());
    Assertions.assertEquals(instance, meta.getInstance());
  }

  @Test
  public void test2() {
    ProducerMeta meta = new ProducerMeta();
    meta.setSchemaId("id1");
    Assertions.assertEquals("id1", meta.getSchemaId());

    meta.setInstance(1);
    Assertions.assertEquals(1, meta.getInstance());
  }
}
