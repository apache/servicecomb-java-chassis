/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.paas.cse.swagger.generator.core;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.huawei.paas.cse.swagger.converter.property.StringPropertyConverter;
import com.huawei.paas.cse.swagger.generator.core.unittest.SwaggerGeneratorForTest;

import io.swagger.converter.ModelConverters;
import io.swagger.models.properties.StringProperty;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author
 * @version  [版本号, 2017年3月23日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class TestProperty {
    SwaggerGeneratorContext context = new DefaultSwaggerGeneratorContext();

    @Test
    public void testStringProperty() {
        SwaggerGenerator generator = new SwaggerGeneratorForTest(context, null);

        List<String> enums = Arrays.asList("testStringProperty_a", "testStringProperty_b");

        StringProperty sp = new StringProperty();
        sp._enum(enums);

        StringPropertyConverter spc = new StringPropertyConverter();
        JavaType jt =
            spc.convert(generator.getClassLoader(), generator.ensureGetPackageName(), generator.getSwagger(), sp);

        StringProperty spNew = (StringProperty) ModelConverters.getInstance().readAsProperty(jt);
        Assert.assertEquals(enums, spNew.getEnum());
    }
}
