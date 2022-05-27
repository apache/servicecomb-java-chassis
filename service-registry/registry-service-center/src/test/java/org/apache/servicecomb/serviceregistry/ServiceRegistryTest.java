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

package org.apache.servicecomb.serviceregistry;

import java.util.regex.Matcher;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServiceRegistryTest {
  @Test
  public void testNameFormat() {
    Matcher matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("abc");
    Assertions.assertTrue(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("abc00");
    Assertions.assertTrue(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("ABC");
    Assertions.assertTrue(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("A2BC");
    Assertions.assertTrue(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("abc-ABC");
    Assertions.assertTrue(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("abc_ABC");
    Assertions.assertTrue(matcher.matches());

    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("");
    Assertions.assertFalse(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("-abc");
    Assertions.assertFalse(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("abc-");
    Assertions.assertFalse(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("_abc");
    Assertions.assertFalse(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("abc_");
    Assertions.assertFalse(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("0abc");
    Assertions.assertFalse(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("ab.c");
    Assertions.assertFalse(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("ab?c");
    Assertions.assertFalse(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("ab#c");
    Assertions.assertFalse(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("ab&c");
    Assertions.assertFalse(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("ab*c");
    Assertions.assertFalse(matcher.matches());
    matcher = ServiceRegistry.REGISTRY_NAME_PATTERN.matcher("ab@c");
    Assertions.assertFalse(matcher.matches());
  }
}
