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

package org.apache.servicecomb.config.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;

public class TestParser {
  @Test
  public void testRawParser() {
    Parser parser = Parser.findParser(Parser.CONTENT_TYPE_RAW);
    assertThat(parser.parse("world", "hello", true)).containsKey("hello").containsValue("world");
    assertThat(parser.parse("world", "hello", false)).containsKey("hello").containsValue("world");
  }

  @Test
  public void testPropertiesParser() {
    Parser parser = Parser.findParser(Parser.CONTENT_TYPE_PROPERTIES);
    assertThat(parser.parse("l1-1=3.0\n"
        + "l1-2=2.0", "hello", true)).containsKeys("hello.l1-1", "hello.l1-2")
        .containsValues("2.0", "3.0");
    assertThat(parser.parse("l1-1=3.0\n"
        + "l1-2=2.0", "hello", false)).containsKeys("l1-1", "l1-2")
        .containsValues("2.0", "3.0");
  }

  @Test
  public void testYamlParser() {
    Parser parser = Parser.findParser(Parser.CONTENT_TYPE_YAML);
    assertThat(parser.parse("l1-1: 3.0\n"
        + "l1-2: 2.0", "hello", true)).containsKeys("hello.l1-1", "hello.l1-2")
        .containsValues("2.0", "3.0");
    assertThat(parser.parse("l1-1: 3.0\n"
        + "l1-2: 2.0", "false", false)).containsKeys("l1-1", "l1-2")
        .containsValues("2.0", "3.0");
  }

  @Test
  public void testInvalidParser() {
    Throwable exception = catchThrowable(() -> Parser.findParser("unknown"));
    assertThat(exception).isInstanceOf(IllegalArgumentException.class);
  }
}
