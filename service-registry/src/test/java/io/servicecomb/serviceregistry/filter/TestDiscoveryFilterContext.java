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

package io.servicecomb.serviceregistry.filter;

import org.junit.Assert;
import org.junit.Test;

public class TestDiscoveryFilterContext {
  DiscoveryFilterContext context = new DiscoveryFilterContext();

  @Test
  public void inputParameters() {
    Object inputParameters = new Object();
    context.setInputParameters(inputParameters);

    Assert.assertSame(inputParameters, context.getInputParameters());
  }

  @Test
  public void contextParameters() {
    String name = "name";
    Object value = new Object();
    context.putContextParameter(name, value);

    Assert.assertSame(value, context.getContextParameter(name));
    Assert.assertNull(context.getContextParameter("notExist"));
  }

  @Test
  public void rerun() {
    Assert.assertEquals(-1, context.popRerunFilter());

    context.setCurrentFilter(1);
    context.pushRerunFilter();

    Assert.assertEquals(1, context.popRerunFilter());
    Assert.assertEquals(-1, context.popRerunFilter());
  }
}
