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

package org.apache.servicecomb.common.rest.codec;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;

public class TestRestObjectMapper {

  @Test
  public void testAutoCloseSource() {
    Assert.assertFalse(RestObjectMapper.INSTANCE.getFactory().isEnabled(Feature.AUTO_CLOSE_SOURCE));
  }

  @Test
  public void testDeserializationFeature() {
    Assert.assertFalse(RestObjectMapper.INSTANCE.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
  }
}
