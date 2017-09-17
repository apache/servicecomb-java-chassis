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

package io.servicecomb.common.rest.codec.param;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.common.rest.codec.param.PathProcessorCreator.PathProcessor;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;

public class TestPathProcessorCreator {
  @Test
  public void testCreate() {
    ParamValueProcessorCreator creator =
        ParamValueProcessorCreatorManager.INSTANCE.findValue(PathProcessorCreator.PARAMTYPE);
    Parameter parameter = new PathParameter();
    parameter.setName("path");

    ParamValueProcessor processor = creator.create(parameter, String.class);

    Assert.assertEquals(PathProcessor.class, processor.getClass());
  }
}
