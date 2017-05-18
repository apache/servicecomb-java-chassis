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

package com.huawei.paas.cse.core.provider.consumer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.huawei.paas.cse.core.Const;
import com.huawei.paas.cse.core.ConsumerProvider;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年12月26日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Component
public class ConsumerProviderManager {
    @Inject
    private List<ConsumerProvider> consumerProviderList;

    // key为微服务名
    private volatile Map<String, ReferenceConfig> referenceConfigMap = new ConcurrentHashMap<>();

    public void init() throws Exception {
        for (ConsumerProvider provider : consumerProviderList) {
            provider.init();
        }
    }

    public ReferenceConfig getReferenceConfig(String microserviceName) {
        ReferenceConfig config = referenceConfigMap.get(microserviceName);
        if (config == null) {
            synchronized (this) {
                config = referenceConfigMap.get(microserviceName);
                if (config == null) {
                    String key = "cse.references." + microserviceName;
                    DynamicStringProperty versionRule = DynamicPropertyFactory.getInstance()
                            .getStringProperty(key + ".version-rule", Const.VERSION_RULE_LATEST);
                    DynamicStringProperty transport =
                        DynamicPropertyFactory.getInstance().getStringProperty(key + ".transport",
                                Const.ANY_TRANSPORT);

                    config = new ReferenceConfig(microserviceName, versionRule.getValue(), transport.getValue());
                    referenceConfigMap.put(microserviceName, config);
                }
            }
        }

        return config;
    }

    // 只用于测试场景
    public ReferenceConfig setTransport(String microserviceName, String transport) {
        ReferenceConfig config = getReferenceConfig(microserviceName);
        config.setTransport(transport);

        return config;
    }
}
