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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.servicecomb.config.ConfigUtil;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.MicroserviceKey;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.WatchAction;
import io.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;

/**
 *
 * @since Mar 14, 2017
 * @see 
 */
public class TestInstanceCacheManager {

    @Before
    public void setUp() throws Exception {
        ConfigUtil.installDynamicConfig();
        RegistryUtils.setSrClient(null);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testInstanceUpdate() {
        Microservice oInstance = RegistryUtils.getMicroservice();
        InstanceCacheManager oInstanceCacheManager = new InstanceCacheManager();
        MicroserviceInstanceChangedEvent oChangedEnvent = new MicroserviceInstanceChangedEvent();
        oChangedEnvent.setAction(WatchAction.UPDATE);
        MicroserviceKey oKey = new MicroserviceKey();
        oKey.setAppId(oInstance.getAppId());
        oKey.setVersion(oInstance.getVersion());
        oKey.setServiceName(oInstance.getServiceName());
        oChangedEnvent.setKey(oKey);
        oChangedEnvent.setInstance(RegistryUtils.getMicroserviceInstance());
        oInstanceCacheManager.onInstanceUpdate(oChangedEnvent);
        oChangedEnvent.setAction(WatchAction.DELETE);
        oInstanceCacheManager.onInstanceUpdate(oChangedEnvent);
        oChangedEnvent.setAction(WatchAction.CREATE);
        oInstanceCacheManager.onInstanceUpdate(oChangedEnvent);
        Assert.assertEquals("UP", RegistryUtils.getMicroserviceInstance().getStatus().toString());
    }
}
