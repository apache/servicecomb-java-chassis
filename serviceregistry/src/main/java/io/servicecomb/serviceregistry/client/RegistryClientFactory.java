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

package io.servicecomb.serviceregistry.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.serviceregistry.client.http.ServiceRegistryClientImpl;

/**
 * Created by   on 2017/3/31.
 */
public final class RegistryClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryClientFactory.class);

    private static final Object LOCK = new Object();

    private static volatile ServiceRegistryClient registryClient;

    private static String localModeFile;

    static {
        reset();
    }

    private RegistryClientFactory() {
    }

    public static String getLocalModeFile() {
        return localModeFile;
    }

    public static ServiceRegistryClient getRegistryClient() {
        if (registryClient != null) {
            return registryClient;
        }

        synchronized (LOCK) {
            if (registryClient != null) {
                return registryClient;
            }

            ServiceRegistryClient client = null;
            if (localModeFile.isEmpty()) {
                LOGGER.info("It is running in the normal mode, a separated service registry is required");
                client = new ServiceRegistryClientImpl();
            } else {
                LOGGER.info(
                        "It is running in the local development mode, the local file {} is using as the local registry",
                        localModeFile);
                client = new LocalServiceRegistryClientImpl();
            }

            try {
                client.init();
            } catch (Exception e) {
                LOGGER.error("init registry client failed.", e);
                return null;
            }

            registryClient = client;
            return registryClient;
        }
    }

    public static void reset() {
        String localFile = System.getProperty("local.registry.file");
        localModeFile = localFile == null ? "" : localFile;
        registryClient = null;
    }
}
