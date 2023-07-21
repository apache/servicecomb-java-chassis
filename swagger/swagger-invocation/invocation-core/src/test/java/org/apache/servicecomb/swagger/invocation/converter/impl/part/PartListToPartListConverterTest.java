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
package org.apache.servicecomb.swagger.invocation.converter.impl.part;

import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.Part;

import org.apache.servicecomb.foundation.common.part.FilePart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PartListToPartListConverterTest {
  PartListToPartListConverter converter = new PartListToPartListConverter();

  @Test
  public void getSrcType() {
    Assertions.assertEquals("java.util.List<jakarta.servlet.http.Part>", converter.getSrcType().getTypeName());
  }

  @Test
  public void getTargetType() {
    Assertions.assertEquals("java.util.List<jakarta.servlet.http.Part>", converter.getTargetType().getTypeName());
  }

  @Test
  public void convert() {
    List<Part> parts = Arrays.asList(new FilePart("name", "file"));
    Assertions.assertSame(parts, converter.convert(parts));
  }
}
