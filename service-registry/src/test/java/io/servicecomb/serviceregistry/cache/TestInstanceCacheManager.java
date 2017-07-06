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

package io.servicecomb.serviceregistry.cache;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.serviceregistry.ServiceRegistry;
import io.servicecomb.serviceregistry.api.MicroserviceKey;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.WatchAction;
import io.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import io.servicecomb.serviceregistry.registry.ServiceRegistryFactory;

/**
 *
 * @since Mar 14, 2017
 * @see 
 */
public class TestInstanceCacheManager {
    @Test
    public void testInstanceUpdate() {
        ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
        Microservice microservice = serviceRegistry.getMicroservice();
        serviceRegistry.init();
        InstanceCacheManager oInstanceCacheManager = serviceRegistry.getInstanceCacheManager();

        MicroserviceInstanceChangedEvent oChangedEnvent = new MicroserviceInstanceChangedEvent();
        oChangedEnvent.setAction(WatchAction.UPDATE);
        MicroserviceKey oKey = new MicroserviceKey();
        oKey.setAppId(microservice.getAppId());
        oKey.setVersion(microservice.getVersion());
        oKey.setServiceName(microservice.getServiceName());
        oChangedEnvent.setKey(oKey);
        oChangedEnvent.setInstance(microservice.getIntance());
        oInstanceCacheManager.onInstanceUpdate(oChangedEnvent);
        oChangedEnvent.setAction(WatchAction.DELETE);
        oInstanceCacheManager.onInstanceUpdate(oChangedEnvent);
        oChangedEnvent.setAction(WatchAction.CREATE);
        oInstanceCacheManager.onInstanceUpdate(oChangedEnvent);
        Assert.assertEquals("UP", microservice.getIntance().getStatus().toString());
    }
}
