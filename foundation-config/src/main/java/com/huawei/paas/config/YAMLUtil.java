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

package com.huawei.paas.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by   on 2017/1/5.
 */
public final class YAMLUtil {
    private YAMLUtil() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> yaml2Properties(InputStream input) {
        Map<String, Object> configurations = new LinkedHashMap<String, Object>();
        Yaml yaml = new Yaml();
        yaml.loadAll(input).forEach(data -> configurations.putAll(retrieveItems("", (Map<String, Object>) data)));
        return configurations;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> retrieveItems(String prefix, Map<String, Object> propertieMap) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        if (!prefix.isEmpty()) {
            prefix += ".";
        }

        for (Map.Entry<String, Object> entry : propertieMap.entrySet()) {
            if (entry.getValue() instanceof Map) {
                result.putAll(retrieveItems(prefix + entry.getKey(), (Map<String, Object>) entry.getValue()));
            } else {
                result.put(prefix + entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public static <T> T[] arrayConcat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
