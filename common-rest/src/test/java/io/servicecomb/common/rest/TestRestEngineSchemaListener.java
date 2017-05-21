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

package io.servicecomb.common.rest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import io.servicecomb.common.rest.locator.ServicePathManager;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.swagger.generator.core.DefaultSwaggerGeneratorContext;
import io.servicecomb.swagger.generator.core.SwaggerGenerator;
import com.huawei.paas.foundation.common.utils.BeanUtils;

import io.swagger.models.Swagger;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author   
 * @version  [版本号, 2017年4月10日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class TestRestEngineSchemaListener {
    DefaultSwaggerGeneratorContext context = new DefaultSwaggerGeneratorContext();

    static class Impl {
        public int add(int x, int y) {
            return 0;
        }
    }

    @Test
    public void test() {
        BeanUtils.setContext(Mockito.mock(ApplicationContext.class));

        MicroserviceMeta mm = new MicroserviceMeta("app:ms");
        List<SchemaMeta> smList = new ArrayList<>();

        SwaggerGenerator generator = new SwaggerGenerator(context, Impl.class);
        Swagger swagger = generator.generate();
        SchemaMeta sm1 = new SchemaMeta(swagger, mm, "sid1");
        smList.add(sm1);

        RestEngineSchemaListener listener = new RestEngineSchemaListener();
        SchemaMeta[] smArr = smList.toArray(new SchemaMeta[smList.size()]);
        listener.onSchemaLoaded(smArr);
        // 重复调用，不应该出异常
        listener.onSchemaLoaded(smArr);

        ServicePathManager spm = ServicePathManager.getServicePathManager(mm);
        Assert.assertEquals(mm, spm.getMicroserviceMeta());

        Assert.assertNotNull(spm.getStaticPathOperationMap().get("Impl/add/"));
    }
}
