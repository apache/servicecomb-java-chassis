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

package com.huawei.paas.cse.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;

import com.huawei.paas.cse.core.provider.consumer.ConsumerProviderManager;
import com.huawei.paas.cse.core.provider.producer.ProducerProviderManager;
import com.huawei.paas.cse.core.transport.TransportManager;
import com.huawei.paas.cse.serviceregistry.RegistryUtils;
import com.huawei.paas.foundation.common.utils.ReflectUtils;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

public class TestCseApplicationListener {
    @Test
    public void testCseApplicationListenerNormal(@Injectable ContextRefreshedEvent event,
            @Injectable AbstractApplicationContext context,
            @Injectable BootListener listener,
            @Injectable ProducerProviderManager producerProviderManager,
            @Injectable ConsumerProviderManager consumerProviderManager,
            @Injectable TransportManager transportManager,
            @Mocked RegistryUtils ru) throws Exception {
        Map<String, BootListener> listeners = new HashMap<>();
        listeners.put("test", listener);

        new Expectations() {
            {
                event.getApplicationContext();
                result = context;
                context.getBeansOfType(BootListener.class);
                result = listeners;
            }
        };

        CseApplicationListener cal = new CseApplicationListener();
        ReflectUtils.setField(cal, "producerProviderManager", producerProviderManager);
        ReflectUtils.setField(cal, "consumerProviderManager", consumerProviderManager);
        ReflectUtils.setField(cal, "transportManager", transportManager);

        cal.onApplicationEvent(event);
    }

    @Test
    public void testCseApplicationListenerThrowException(@Injectable ContextRefreshedEvent event,
            @Injectable AbstractApplicationContext context,
            @Injectable BootListener listener,
            @Injectable ProducerProviderManager producerProviderManager) throws Exception {
        Map<String, BootListener> listeners = new HashMap<>();
        listeners.put("test", listener);

        new Expectations() {
            {
                event.getApplicationContext();
                result = context;
                context.getBeansOfType(BootListener.class);
                result = listeners;
                producerProviderManager.init();
                result = new IOException();
            }
        };
        CseApplicationListener cal = new CseApplicationListener();
        ReflectUtils.setField(cal, "producerProviderManager", producerProviderManager);
        cal.onApplicationEvent(event);
    }

    @Test
    public void testCseApplicationListenerParentNotnull(@Injectable ContextRefreshedEvent event,
            @Injectable AbstractApplicationContext context,
            @Injectable AbstractApplicationContext pContext) throws Exception {

        new Expectations() {
            {
                event.getApplicationContext();
                result = context;
            }
        };
        CseApplicationListener cal = new CseApplicationListener();
        cal.onApplicationEvent(event);
    }

    @Test
    public void testCseApplicationListenerShutdown(@Injectable ContextClosedEvent event,
            @Mocked RegistryUtils ru) throws Exception {
        new Expectations() {
            {
                RegistryUtils.destory();
            }
        };
        CseApplicationListener cal = new CseApplicationListener();
        cal.onApplicationEvent(event);
    }
}
