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

package io.servicecomb.swagger.generator.jaxrs;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.swagger.generator.core.CompositeSwaggerGeneratorContext;
import io.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import io.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 *
 * @version  [版本号, 2017年3月27日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class TestJaxrs {
    SwaggerGeneratorContext context = new JaxrsSwaggerGeneratorContext();

    @Test
    public void testResponse() throws Exception {
        UnitTestSwaggerUtils.testSwagger("schemas/response.yaml", context, Echo.class, "response");
    }

    @Test
    public void testInvalidResponse() throws Exception {
        UnitTestSwaggerUtils.testException("Use ApiOperation or ApiResponses to declare response type",
                context,
                Echo.class,
                "invalidResponse");
    }

    @Test
    public void testEcho() throws Exception {
        UnitTestSwaggerUtils.testSwagger("schemas/echo.yaml", context, Echo.class, "echo");
    }

    @Test
    public void testForm() throws Exception {
        UnitTestSwaggerUtils.testSwagger("schemas/form.yaml", context, Echo.class, "form");
    }

    @Test
    public void testQuery() throws Exception {
        UnitTestSwaggerUtils.testSwagger("schemas/query.yaml", context, Echo.class, "query");
    }

    @Test
    public void testQueryComplex() throws Exception {
        UnitTestSwaggerUtils.testException(
                "not allow complex type for query parameter, method=io.servicecomb.swagger.generator.jaxrs.Echo:queryComplex, paramIdx=0, type=java.util.List<io.servicecomb.swagger.generator.jaxrs.User>",
                context,
                Echo.class,
                "queryComplex");
    }

    @Test
    public void testCookie() throws Exception {
        UnitTestSwaggerUtils.testSwagger("schemas/cookie.yaml", context, Echo.class, "cookie");
    }

    @Test
    public void testEmptyPath() throws Exception {
        UnitTestSwaggerUtils.testSwagger("schemas/emptyPath.yaml", context, Echo.class, "emptyPath");
    }

    @Test
    public void testComposite() {
        CompositeSwaggerGeneratorContext composite = new CompositeSwaggerGeneratorContext();
        SwaggerGeneratorContext context = composite.selectContext(Echo.class);

        Assert.assertEquals(JaxrsSwaggerGeneratorContext.class, context.getClass());
    }
}
