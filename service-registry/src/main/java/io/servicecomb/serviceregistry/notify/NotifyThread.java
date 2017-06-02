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

package io.servicecomb.serviceregistry.notify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.servicecomb.foundation.common.CommonThread;

/**
 * Created by   on 2017/3/12.
 */
public class NotifyThread extends CommonThread {
    private static final int TIMEOUT = 1;

    public NotifyThread() {
        super();
        setName("NotifyThread");
    }

    @Override
    public void run() {
        while (isRunning()) {
            List<RegistryMessage> messages = new ArrayList<>();

            for (RegistryMessage message : NotifyManager.INSTANCE) {
                messages.add(message);
            }

            for (RegistryMessage message : messages) {
                NotifyManager.INSTANCE.notifyListeners(message.getEvent(), message.getPayload());
            }

            try {
                TimeUnit.SECONDS.sleep(TIMEOUT);
            } catch (InterruptedException e) {
            }
        }
    }
}
