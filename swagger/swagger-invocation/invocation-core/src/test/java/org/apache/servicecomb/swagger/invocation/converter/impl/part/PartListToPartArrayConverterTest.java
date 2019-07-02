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

import javax.servlet.http.Part;

import org.apache.servicecomb.foundation.common.part.FilePart;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class PartListToPartArrayConverterTest {
  PartListToPartArrayConverter converter = new PartListToPartArrayConverter();

  @Test
  public void getSrcType() {
    Assert.assertEquals("java.util.List<javax.servlet.http.Part>", converter.getSrcType().getTypeName());
  }

  @Test
  public void getTargetType() {
    Assert.assertEquals(Part[].class.getCanonicalName(), converter.getTargetType().getTypeName());
  }

  @Test
  public void convert() {
    Object parts = converter.convert(Arrays.asList(new FilePart("name", "file")));
    Assert.assertThat(parts, Matchers.instanceOf(Part[].class));
  }

  @Test
  public void should_got_null_when_convert_null() {
    Assert.assertNull(converter.convert(null));
  }
}