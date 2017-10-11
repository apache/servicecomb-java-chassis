/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.common.rest.codec;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.junit.Assert;
import org.junit.Test;

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
