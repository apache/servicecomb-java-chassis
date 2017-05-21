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

package io.servicecomb.provider.pojo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.foundation.common.utils.ReflectUtils;
import io.servicecomb.provider.common.MockUtil;
import io.servicecomb.provider.pojo.PojoConsumerProvider;
import io.servicecomb.provider.pojo.reference.PojoConsumers;
import io.servicecomb.provider.pojo.reference.PojoReferenceMeta;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;

public class TestPojoConsumerProvider {

    public String fieldTest = "test";

    /**
     * Test Init
     * @throws Exception 
     */
    @Test
    public void testInit() throws Exception {
        MockUtil.getInstance().mockMicroserviceMeta();
        MockUtil.getInstance().mockRegisterManager();
        MockUtil.getInstance().mockSchemaMeta();
        MockUtil.getInstance().mockConsumerProviderManager();

        PojoConsumerProvider pojoConsumerProvider = new PojoConsumerProvider();
        ReflectUtils.setField(pojoConsumerProvider, "pojoConsumers", new PojoConsumers());
        pojoConsumerProvider.init();
        Assert.assertEquals("pojo", pojoConsumerProvider.getName());
    }

    @Test
    public void testpojoConusmersNotNUll(@Injectable PojoConsumers consumser,
            @Injectable PojoReferenceMeta meta) throws Exception {
        List<PojoReferenceMeta> metas = new ArrayList<>();
        metas.add(meta);
        new Expectations() {
            {
                consumser.getConsumerList();
                result = metas;
            }
        };
        PojoConsumerProvider pojoConsumerProvider = new PojoConsumerProvider();
        Deencapsulation.setField(pojoConsumerProvider, "pojoConsumers", consumser);
        pojoConsumerProvider.init();
    }
}
