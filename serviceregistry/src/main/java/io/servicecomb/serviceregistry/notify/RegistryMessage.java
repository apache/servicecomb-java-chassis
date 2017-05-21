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

/**
 * Created by   on 2017/3/12.
 */
public class RegistryMessage {
    private RegistryEvent event;

    private Object payload;

    public RegistryMessage() {

    }

    public RegistryMessage(RegistryEvent evt, Object payload) {
        this.event = evt;
        this.payload = payload;
    }

    public RegistryEvent getEvent() {
        return event;
    }

    public void setEvent(RegistryEvent event) {
        this.event = event;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
