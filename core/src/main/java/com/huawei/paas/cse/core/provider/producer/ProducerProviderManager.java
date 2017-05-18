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

package com.huawei.paas.cse.core.provider.producer;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.huawei.paas.cse.core.ProducerProvider;
import com.huawei.paas.cse.core.definition.MicroserviceMeta;
import com.huawei.paas.cse.core.definition.MicroserviceMetaManager;
import com.huawei.paas.cse.core.definition.SchemaMeta;
import com.huawei.paas.cse.core.definition.SchemaUtils;
import com.huawei.paas.cse.serviceregistry.RegistryUtils;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年11月29日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Component
public class ProducerProviderManager {
    @Inject
    private List<ProducerProvider> producerProviderList;

    @Inject
    private MicroserviceMetaManager microserviceMetaManager;

    public void init() throws Exception {
        for (ProducerProvider provider : producerProviderList) {
            provider.init();
        }

        MicroserviceMeta microserviceMeta =
            microserviceMetaManager.getOrCreateMicroserviceMeta(RegistryUtils.getMicroservice().getServiceName());
        for (SchemaMeta schemaMeta : microserviceMeta.getSchemaMetas()) {
            String content = SchemaUtils.swaggerToString(schemaMeta.getSwagger());
            RegistryUtils.getMicroservice().addSchema(schemaMeta.getSchemaId(), content);
        }
    }
}
