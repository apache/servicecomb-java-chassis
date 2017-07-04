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

package io.servicecomb.transport.rest.servlet;

import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

public final class ServletConfig {

    private static final long DEFAULT_TIMEOUT = 3000;

    private static final String DEFAULT_LISTEN_ADDRESS = "0.0.0.0:8080";

    private ServletConfig() {
    }

    public static long getServerTimeout() {
        DynamicLongProperty address =
            DynamicPropertyFactory.getInstance().getLongProperty("cse.rest.server.timeout", DEFAULT_TIMEOUT);
        return address.get();
    }

    public static String getLocalServerAddress() {
        DynamicStringProperty address =
            DynamicPropertyFactory.getInstance().getStringProperty("cse.rest.address", DEFAULT_LISTEN_ADDRESS);
        return address.get();
    }
}
