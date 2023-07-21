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

import java.io.ByteArrayInputStream;

import jakarta.servlet.http.Part;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

public class TestResourceToPartConverter {
  ResourceToPartConverter converter = new ResourceToPartConverter();

  @Test
  public void getSrcType() {
    Assertions.assertEquals(Resource.class.getName(), converter.getSrcType().getTypeName());
  }

  @Test
  public void getTargetType() {
    Assertions.assertEquals(Part.class.getName(), converter.getTargetType().getTypeName());
  }

  @Test
  public void convert() {
    Object part = converter.convert(new InputStreamResource(new ByteArrayInputStream(new byte[] {})));
    MatcherAssert.assertThat(part, Matchers.instanceOf(Part.class));
  }
}
