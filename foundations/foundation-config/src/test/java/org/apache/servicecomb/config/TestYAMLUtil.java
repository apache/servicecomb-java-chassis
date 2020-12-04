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

package org.apache.servicecomb.config;

import org.junit.Assert;
import org.junit.Test;

public class TestYAMLUtil {
  public static class Person {
    String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public static class UnsafePerson {
    String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  @Test
  public void testSafeParser() {
    Person person = YAMLUtil.parserObject("name: hello", Person.class);
    Assert.assertEquals("hello", person.getName());

    person = YAMLUtil.parserObject("!!org.apache.servicecomb.config.TestYAMLUtil$Person\n"
        + "name: hello", Person.class);
    Assert.assertEquals("hello", person.getName());

    person = YAMLUtil.parserObject("!!org.apache.servicecomb.config.TestYAMLUtil$UnsafePerson\n"
        + "name: hello", Person.class);
    Assert.assertEquals("hello", person.getName());

    // using Object.class is not safe, do not used in product code.
    Object object = YAMLUtil.parserObject("!!org.apache.servicecomb.config.TestYAMLUtil$UnsafePerson\n"
        + "name: hello", Object.class);
    Assert.assertEquals("hello", ((UnsafePerson) object).getName());
  }
}
