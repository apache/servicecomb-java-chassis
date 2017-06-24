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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

import io.servicecomb.config.ConfigUtil;
import io.servicecomb.config.archaius.sources.ConfigModel;

public class MicroserviceDefinitionManager {
    // not allow multi appId in one process
    private String appId;

    // key is microserviceName
    private Map<String, MicroserviceDefinition> definitionMap = new HashMap<>();

    public String getAppId() {
        return appId;
    }

    public Map<String, MicroserviceDefinition> getDefinitionMap() {
        return definitionMap;
    }

    public void init(List<ConfigModel> sortedConfigModelList) {
        Set<String> nameSet = collectMicroserviceName(sortedConfigModelList);
        Map<String, List<ConfigModel>> group = createConfigModelGroup(nameSet, sortedConfigModelList);
        createDefinitions(group);
        writeAppId();
    }

    private void createDefinitions(Map<String, List<ConfigModel>> group) {
        for (List<ConfigModel> configModelList : group.values()) {
            MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(configModelList);
            initAppId(microserviceDefinition);
            definitionMap.put(microserviceDefinition.getMicroserviceName(), microserviceDefinition);
        }

        // check after placeholder resolved, if there are duplicate names
        if (definitionMap.size() != group.size()) {
            throw new IllegalArgumentException(
                    String.format("Create from %s, but get %s",
                            group.keySet().toString(),
                            definitionMap.keySet().toString()));
        }
    }

    // collect name from org config data
    // name maybe a var, eg: ${name}
    private Set<String> collectMicroserviceName(List<ConfigModel> sortedConfigModelList) {
        Set<String> nameSet = new HashSet<>();
        for (ConfigModel configModel : sortedConfigModelList) {
            String name = readMicroserviceName(configModel);
            if (StringUtils.isEmpty(name) || ConfigUtil.isAdditionalConfig(configModel)) {
                continue;
            }

            nameSet.add(name);
        }
        return nameSet;
    }

    private void writeAppId() {
        if (appId == null) {
            appId = DefinitionConst.defaultAppId;
        }

        for (MicroserviceDefinition microserviceDefinition : definitionMap.values()) {
            microserviceDefinition.getConfiguration().setProperty(DefinitionConst.appIdKey, appId);
        }
    }

    private void initAppId(MicroserviceDefinition microserviceDefinition) {
        String tmpAppId = microserviceDefinition.getConfiguration().getString(DefinitionConst.appIdKey);
        if (StringUtils.isEmpty(tmpAppId)) {
            return;
        }

        if (appId == null) {
            appId = tmpAppId;
            return;
        }

        if (!appId.equals(tmpAppId)) {
            throw new IllegalArgumentException(
                    String.format("Not allowed multiple appId in one process, but have %s and %s.", appId, tmpAppId));
        }
    }

    private Map<String, List<ConfigModel>> createConfigModelGroup(Set<String> nameSet,
            List<ConfigModel> sortedConfigModelList) {
        // ignore appId
        Map<String, List<ConfigModel>> groups = new HashMap<>();

        for (String name : nameSet) {
            List<ConfigModel> configList = new ArrayList<>();
            groups.put(name, configList);

            // rule:
            // 1.common config: microserviceName is empty
            // 2.additional config, this is for compatible reason.
            // 3.name equals 
            for (int idx = 0; idx < sortedConfigModelList.size(); idx++) {
                ConfigModel configModel = sortedConfigModelList.get(idx);
                String microserviceName = readMicroserviceName(configModel);
                if (StringUtils.isEmpty(microserviceName) || name.equals(microserviceName)
                        || ConfigUtil.isAdditionalConfig(configModel)) {
                    configList.add(configModel);
                    continue;
                }
            }
        }

        return groups;
    }

    private String readMicroserviceName(ConfigModel configModel) {
        @SuppressWarnings("unchecked")
        Map<String, Object> serviceDesc =
            (Map<String, Object>) configModel.getConfig().get(DefinitionConst.serviceDescriptionKey);
        if (serviceDesc == null) {
            return null;
        }

        return (String) serviceDesc.get(DefinitionConst.nameKey);
    }
}
