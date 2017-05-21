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

package io.servicecomb.swagger.generator.core;

import java.lang.reflect.Method;
import java.util.List;

import javax.ws.rs.Path;

import io.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import io.servicecomb.swagger.generator.core.utils.ClassUtils;
import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.swagger.generator.core.schema.User;

import io.swagger.annotations.SwaggerDefinition;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author
 * @version  [版本号, 2017年4月1日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@SwaggerDefinition
public class TestClassUtils {
    @Test
    public void testHasAnnotation() {
        Assert.assertEquals(true, ClassUtils.hasAnnotation(TestClassUtils.class, SwaggerDefinition.class));
        Assert.assertEquals(true, ClassUtils.hasAnnotation(TestClassUtils.class, Test.class));

        Assert.assertEquals(false, ClassUtils.hasAnnotation(TestClassUtils.class, Path.class));
    }

    public static class Impl {
        public List<User> getUser(List<String> names) {
            return null;
        }
    }

    @Test
    public void testCreateInterface() {
        SwaggerGenerator generator = UnitTestSwaggerUtils.generateSwagger(Impl.class);
        Class<?> intf = ClassUtils.getOrCreateInterface(generator);

        Assert.assertEquals("gen.swagger.ImplIntf", intf.getName());
        Assert.assertEquals(1, intf.getMethods().length);

        Method method = intf.getMethods()[0];
        Assert.assertEquals("getUser", method.getName());

        Assert.assertEquals("gen.swagger.names", method.getGenericParameterTypes()[0].getTypeName());
        Assert.assertEquals("java.util.List<io.servicecomb.swagger.generator.core.schema.User>",
                method.getGenericReturnType().getTypeName());
    }
}
