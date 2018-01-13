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

package org.apache.servicecomb.foundation.common.part;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public class TestResourcePart {
  String name = "paramName";

  byte[] bytes = new byte[] {1, 2, 3};

  Resource resource = new ByteArrayResource(bytes);

  ResourcePart part = new ResourcePart(name, resource);

  @Test
  public void getName() {
    Assert.assertEquals(name, part.getName());
  }

  @Test
  public void getInputStream() throws IOException {
    try (InputStream is = part.getInputStream()) {
      byte[] content = IOUtils.toByteArray(is);
      Assert.assertArrayEquals(bytes, content);
    }
  }
}
