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

package io.servicecomb.serviceregistry.definition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.servicecomb.config.ConfigUtil;
import io.servicecomb.config.archaius.sources.ConfigModel;

public class MicroserviceDefinition {
    private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceDefinition.class);

    // microservice maybe combined from many microservices
    // if a and b combined to ab, then combinedFrom value is a,b
    // if not combined, just only one microservice, then combinedFrom is empty
    private Set<String> combinedFrom = new HashSet<>();

    private List<ConfigModel> configModels;

    private Configuration configuration;

    private String microserviceName;

    public String getMicroserviceName() {
        return microserviceName;
    }

    public static MicroserviceDefinition create(String appId, String microserviceName) {
        ConfigModel configModel = createConfigModel(appId, microserviceName);
        return new MicroserviceDefinition(Arrays.asList(configModel));
    }

    public static ConfigModel createConfigModel(String appId, String microserviceName) {
        Map<String, Object> descMap = new HashMap<>();
        descMap.put(DefinitionConst.nameKey, microserviceName);

        Map<String, Object> config = new HashMap<>();
        config.put(DefinitionConst.appIdKey, appId);
        config.put(DefinitionConst.serviceDescriptionKey, descMap);

        ConfigModel configModel = new ConfigModel();
        configModel.setConfig(config);
        return configModel;
    }

    public MicroserviceDefinition(List<ConfigModel> configModels) {
        if (configModels == null) {
            configModels = Collections.emptyList();
        }

        this.configModels = configModels;
        this.configuration = ConfigUtil.createConfig(configModels);
        this.microserviceName =
            configuration.getString(DefinitionConst.qulifiedServiceNameKey, DefinitionConst.defaultMicroserviceName);

        // log paths first, even microserviceName is invalid, this can help user to find problems
        logConfigPath();
        checkMicroserviceName(microserviceName);

        initCombinedFrom(configModels);
    }

    public Set<String> getCombinedFrom() {
        return combinedFrom;
    }

    private void initCombinedFrom(List<ConfigModel> configModels) {
        for (ConfigModel model : configModels) {
            Configuration conf = ConfigUtil.createConfig(Arrays.asList(model));
            String name =
                conf.getString(DefinitionConst.qulifiedServiceNameKey, DefinitionConst.defaultMicroserviceName);
            checkMicroserviceName(name);
            combinedFrom.add(name);
        }

        combinedFrom.remove(microserviceName);
    }

    private void checkMicroserviceName(String name) {
        // the configuration we used
        // when resolve placeholder failed
        // the result will remains ${var}
        if (StringUtils.isEmpty(name) || name.contains("${")) {
            throw new IllegalArgumentException(String.format(
                    "MicroserviceName '%s' is invalid. you must configure '%s' or set the placeholder value.",
                    name,
                    DefinitionConst.qulifiedServiceNameKey));
        }
    }

    // microserviceName maybe null
    public void logConfigPath() {
        List<String> pathList = new ArrayList<>();
        for (ConfigModel configModel : configModels) {
            if (configModel.getUrl() != null) {
                pathList.add(configModel.getUrl().toString());
            }
        }
        LOGGER.info("load microservice config, name={}, paths={}",
                microserviceName,
                pathList);
    }

    public List<ConfigModel> getConfigModels() {
        return configModels;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
