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

package io.servicecomb.serviceregistry.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.AbstractConfiguration;

import com.netflix.config.DynamicPropertyFactory;

public final class ConfigurePropertyUtils {
    private ConfigurePropertyUtils() {
    }

    /**
     * 获取key包含prefix前缀的所有配置项
     * @param prefix
     * @return
     */
    public static Map<String, String> getPropertiesWithPrefix(String prefix) {
        Map<String, String> propertiesMap = new HashMap<>();

        Object config = DynamicPropertyFactory.getBackingConfigurationSource();
        if (null != config && AbstractConfiguration.class.isInstance(config)) {
            AbstractConfiguration composite = (AbstractConfiguration) config;
            Iterator<String> keysIterator = composite.getKeys(prefix);
            while (keysIterator.hasNext()) {
                String key = keysIterator.next();
                propertiesMap.put(key.substring(prefix.length() + 1), String.valueOf(composite.getProperty(key)));
            }
        }
        return propertiesMap;
    }
}
