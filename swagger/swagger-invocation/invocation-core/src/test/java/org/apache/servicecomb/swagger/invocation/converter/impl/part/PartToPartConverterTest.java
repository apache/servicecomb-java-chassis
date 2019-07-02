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

import javax.servlet.http.Part;

import org.apache.servicecomb.foundation.common.part.FilePart;
import org.junit.Assert;
import org.junit.Test;

public class PartToPartConverterTest {
  PartToPartConverter converter = new PartToPartConverter();

  @Test
  public void getSrcType() {
    Assert.assertEquals(Part.class.getName(), converter.getSrcType().getTypeName());
  }

  @Test
  public void getTargetType() {
    Assert.assertEquals(Part.class.getName(), converter.getTargetType().getTypeName());
  }

  @Test
  public void convert() {
    Part part = new FilePart("name", "file");
    Assert.assertSame(part, converter.convert(part));
  }
}