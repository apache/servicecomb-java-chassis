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

package com.huawei.paas.cse.serviceregistry.notify;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.paas.cse.serviceregistry.api.response.MicroserviceInstanceChangedEvent;

/**
 * Created by   on 2017/3/12.
 */
public class NotifyManager implements Iterable<RegistryMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyManager.class);

    public static final NotifyManager INSTANCE = new NotifyManager();

    private List<RegistryMessage> queue = new ArrayList<>();

    private List<RegistryListener> listeners = new ArrayList<>();

    private final Object lock = new Object();

    private final Object queueLock = new Object();

    public void addListener(RegistryListener listener) {
        synchronized (lock) {
            listeners.add(listener);
        }
    }

    public void notify(RegistryMessage message) {
        synchronized (queueLock) {
            queue.add(message);
        }
    }

    public void notifyListeners(RegistryEvent evt, Object obj) {
        synchronized (lock) {
            for (RegistryListener listener : listeners) {
                try {
                    switch (evt) {
                        case INITIALIZED:
                            listener.onInitialized();
                            break;
                        case INSTANCE_CHANGED:
                            listener.onMicroserviceInstanceChanged((MicroserviceInstanceChangedEvent) obj);
                            break;
                        case HEARTBEAT:
                            listener.onHeartbeat();
                            break;
                        case EXCEPTION:
                            listener.onException((Throwable) obj);
                            break;
                        case RECOVERED:
                            listener.onRecovered();
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    LOGGER.error("notify service center listener fail", e);
                }
            }
        }
    }

    public void notify(RegistryEvent event, Object payload) {
        notify(new RegistryMessage(event, payload));
    }

    private class RegistryMessageIterator implements Iterator<RegistryMessage> {
        @Override
        public boolean hasNext() {
            synchronized (queueLock) {
                return queue.size() > 0;
            }
        }

        @Override
        public RegistryMessage next() {
            synchronized (queueLock) {
                return queue.remove(0);
            }
        }
    }

    @Override
    public Iterator<RegistryMessage> iterator() {
        return new RegistryMessageIterator();
    }
}
