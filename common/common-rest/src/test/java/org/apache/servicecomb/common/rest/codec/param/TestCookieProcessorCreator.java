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

package org.apache.servicecomb.common.rest.codec.param;

import org.apache.servicecomb.common.rest.codec.param.CookieProcessorCreator.CookieProcessor;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.models.parameters.CookieParameter;

public class TestCookieProcessorCreator {
  @Test
  public void testCreate() {
    ParamValueProcessorCreator creator =
        ParamValueProcessorCreatorManager.INSTANCE.findValue(CookieProcessorCreator.PARAMTYPE);
    CookieParameter p = new CookieParameter();
    p.setName("p1");

    ParamValueProcessor processor = creator.create(p, String.class);

    Assert.assertEquals(CookieProcessor.class, processor.getClass());
  }
}
