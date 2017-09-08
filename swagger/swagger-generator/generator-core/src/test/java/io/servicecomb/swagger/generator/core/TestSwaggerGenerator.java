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

package io.servicecomb.swagger.generator.core;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StringValueResolver;

import io.servicecomb.foundation.test.scaffolding.spring.SpringUtils;
import io.servicecomb.swagger.generator.pojo.PojoSwaggerGeneratorContext;

public class TestSwaggerGenerator {
  @Test
  public void testBasePathPlaceHolder() {
    StringValueResolver stringValueResolver =
        SpringUtils.createStringValueResolver(Collections.singletonMap("var", "varValue"));

    PojoSwaggerGeneratorContext context = new PojoSwaggerGeneratorContext();
    context.setEmbeddedValueResolver(stringValueResolver);

    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(context, null);
    swaggerGenerator.setBasePath("/a/${var}/b");

    Assert.assertEquals("/a/varValue/b", swaggerGenerator.getSwagger().getBasePath());
  }
}
