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

import javax.inject.Inject;

import io.servicecomb.provider.pojo.reference.PojoConsumers;
import io.servicecomb.provider.pojo.reference.PojoReferenceMeta;
import org.springframework.stereotype.Component;

import io.servicecomb.core.provider.consumer.AbstractConsumerProvider;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年11月30日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Component
public class PojoConsumerProvider extends AbstractConsumerProvider {
    @Inject
    private PojoConsumers pojoConsumers;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return PojoConst.POJO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() throws Exception {
        for (PojoReferenceMeta pojoReference : pojoConsumers.getConsumerList()) {
            pojoReference.createInvoker();
        }
    }
}
