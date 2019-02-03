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
package org.apache.servicecomb.config.inject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPlaceholderResolver {
  static Map<String, Object> parameters = new HashMap<>();

  static PlaceholderResolver resolver = new PlaceholderResolver();

  @BeforeClass
  public static void setup() {
    parameters.put("key", "value");
    parameters.put("varOfVar", Arrays.asList("${key}"));

    parameters.put("priority", "low");

    parameters.put("low-list", Arrays.asList("low-1", "low-2"));
    parameters.put("middle-list", Arrays.asList("middle-1", "middle-2"));
    parameters.put("high-list", Arrays.asList("high-1", "high-2"));
  }

  @Test
  public void unknown() {
    Assert.assertThat(resolver.replace("prefix${xxx}suffix", parameters),
        Matchers.contains("prefix${xxx}suffix"));
  }

  @Test
  public void empty() {
    Assert.assertThat(resolver.replace("prefix${}suffix", parameters),
        Matchers.contains("prefix${}suffix"));
  }

  @Test
  public void notComplete() {
    Assert.assertThat(resolver.replace("prefix${suffix", parameters),
        Matchers.contains("prefix${suffix"));
  }

  @Test
  public void normal() {
    Assert.assertThat(resolver.replace("prefix.${key}.suffix", parameters),
        Matchers.contains("prefix.value.suffix"));
  }

  @Test
  public void disable() {
    Assert.assertThat(resolver.replace("prefix.\\${key}.suffix", parameters),
        Matchers.contains("prefix.${key}.suffix"));
  }

  @Test
  public void varOfVar() {
    Assert.assertThat(resolver.replace("prefix.${varOfVar}.suffix", parameters),
        Matchers.contains("prefix.value.suffix"));
  }

  @Test
  public void list() {
    Assert.assertThat(resolver.replace("prefix.${low-list}.suffix", parameters),
        Matchers.contains("prefix.low-1.suffix", "prefix.low-2.suffix"));
  }

  @Test
  public void multi_list() {
    Assert.assertThat(resolver.replace("prefix.${low-list}.${middle-list}.${high-list}.suffix", parameters),
        Matchers.contains(
            "prefix.low-1.middle-1.high-1.suffix",
            "prefix.low-1.middle-1.high-2.suffix",
            "prefix.low-1.middle-2.high-1.suffix",
            "prefix.low-1.middle-2.high-2.suffix",
            "prefix.low-2.middle-1.high-1.suffix",
            "prefix.low-2.middle-1.high-2.suffix",
            "prefix.low-2.middle-2.high-1.suffix",
            "prefix.low-2.middle-2.high-2.suffix"));
  }

  @Test
  public void nested() {
    Assert.assertThat(resolver.replace("prefix.${${priority}-list}.suffix", parameters),
        Matchers.contains("prefix.low-1.suffix", "prefix.low-2.suffix"));
  }

  @Test
  public void mixed() {
    Assert.assertThat(resolver.replace("prefix.${${priority}-list}.${key}.${high-list}.suffix ${xxx}", parameters),
        Matchers.contains(
            "prefix.low-1.value.high-1.suffix ${xxx}",
            "prefix.low-2.value.high-1.suffix ${xxx}",
            "prefix.low-1.value.high-2.suffix ${xxx}",
            "prefix.low-2.value.high-2.suffix ${xxx}"));
  }
}
