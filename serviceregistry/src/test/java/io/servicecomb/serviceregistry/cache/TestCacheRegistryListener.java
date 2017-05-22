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

import io.servicecomb.serviceregistry.api.registry.WatchAction;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.MicroserviceKey;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;

import mockit.Mock;
import mockit.MockUp;

public class TestCacheRegistryListener {

    @BeforeClass
    public static void setUp() throws Exception {
        new MockUp<RegistryUtils>() {
            @Mock
            Microservice createMicroserviceFromDefinition() {
                return Mockito.mock(Microservice.class);
            }
        };
    }

    CacheRegistryListener instance = new CacheRegistryListener();

    @Test
    public void testonMicroserviceInstanceChangedDELETE() {
        MicroserviceInstanceChangedEvent changedEvent = new MicroserviceInstanceChangedEvent();
        changedEvent.setAction(WatchAction.DELETE);
        MicroserviceKey key = Mockito.mock(MicroserviceKey.class);
        changedEvent.setKey(key);
        MicroserviceInstance mInstance = Mockito.mock(MicroserviceInstance.class);
        changedEvent.setInstance(mInstance);
        instance.onMicroserviceInstanceChanged(changedEvent);

    }

    @Test
    public void testonMicroserviceInstanceChangedUPDATE() {
        MicroserviceInstanceChangedEvent changedEvent = new MicroserviceInstanceChangedEvent();
        changedEvent.setAction(WatchAction.UPDATE);
        MicroserviceKey key = Mockito.mock(MicroserviceKey.class);
        changedEvent.setKey(key);
        MicroserviceInstance mInstance = Mockito.mock(MicroserviceInstance.class);
        changedEvent.setInstance(mInstance);
        instance.onMicroserviceInstanceChanged(changedEvent);

    }

    @Test
    public void testonMicroserviceInstanceChangedCREATE() {
        MicroserviceInstanceChangedEvent changedEvent = new MicroserviceInstanceChangedEvent();
        changedEvent.setAction(WatchAction.CREATE);
        MicroserviceKey key = Mockito.mock(MicroserviceKey.class);
        changedEvent.setKey(key);
        MicroserviceInstance mInstance = Mockito.mock(MicroserviceInstance.class);
        changedEvent.setInstance(mInstance);
        instance.onMicroserviceInstanceChanged(changedEvent);

    }

    @Test
    public void testonOnRecovered() {
        instance.onRecovered();
    }
}
