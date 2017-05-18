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

package com.huawei.paas.cse.serviceregistry.cache;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.huawei.paas.cse.serviceregistry.RegistryUtils;
import com.huawei.paas.cse.serviceregistry.api.MicroserviceKey;
import com.huawei.paas.cse.serviceregistry.api.registry.Microservice;
import com.huawei.paas.cse.serviceregistry.api.registry.WatchAction;
import com.huawei.paas.cse.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import com.huawei.paas.foundation.common.utils.BeanUtils;
import com.huawei.paas.foundation.common.utils.Log4jUtils;

/**
 * @author  
 * @since Mar 14, 2017
 * @see 
 */
public class TestInstanceCacheManager {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Log4jUtils.init();
        BeanUtils.init();
        RegistryUtils.setSrClient(null);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test InstanceUpdate
     */
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
