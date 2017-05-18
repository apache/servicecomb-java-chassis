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

import com.huawei.paas.cse.serviceregistry.api.response.MicroserviceInstanceChangedEvent;

/**
 * Created by   on 2017/3/12.
 */
public interface RegistryListener {
    void onInitialized();

    void onMicroserviceInstanceChanged(MicroserviceInstanceChangedEvent evt);

    void onHeartbeat();

    void onException(Throwable e);

    void onRecovered();
}
