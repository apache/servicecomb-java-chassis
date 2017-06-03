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

package io.servicecomb.core.provider.consumer;

import io.servicecomb.core.Const;
import io.servicecomb.core.CseContext;
import io.servicecomb.core.definition.MicroserviceMeta;

public class ReferenceConfig {
    private MicroserviceMeta microserviceMeta;

    private String microserviceVersionRule = Const.DEFAULT_VERSION_RULE;

    private String transport = Const.ANY_TRANSPORT;

    public ReferenceConfig() {
    }

    public ReferenceConfig(String microserviceName, String microserviceVersionRule, String transport) {
        this.microserviceMeta =
            CseContext.getInstance().getConsumerSchemaFactory().getOrCreateConsumer(microserviceName,
                    microserviceVersionRule);

        this.microserviceVersionRule = microserviceVersionRule;
        this.transport = transport;
    }

    public MicroserviceMeta getMicroserviceMeta() {
        return microserviceMeta;
    }

    public String getMicroserviceVersionRule() {
        return microserviceVersionRule;
    }

    public void setMicroserviceVersionRule(String microserviceVersionRule) {
        this.microserviceVersionRule = microserviceVersionRule;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }
}
