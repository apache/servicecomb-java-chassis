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
package io.servicecomb.serviceregistry.api.registry;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;
import org.springframework.util.StringUtils;

import io.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import io.servicecomb.foundation.common.utils.FileUtils;
import io.servicecomb.serviceregistry.config.InstancePropertiesLoader;
import io.servicecomb.serviceregistry.config.MicroservicePropertiesLoader;
import io.servicecomb.serviceregistry.definition.DefinitionConst;
import io.servicecomb.serviceregistry.definition.MicroserviceDefinition;
import io.servicecomb.serviceregistry.definition.MicroserviceDefinitionManager;

public class MicroserviceManager {
    // not allow multi appId in one process
    private String appId;

    private Map<String, Microservice> nameMap = new ConcurrentHashMap<>();

    private Map<String, Microservice> rootMap = new ConcurrentHashMap<>();

    // if there is only one microservice in one process
    // then defaultMicroservice is not null
    private Microservice defaultMicroservice;

    public String getAppId() {
        return appId;
    }

    public void init(MicroserviceConfigLoader loader) {
        MicroserviceDefinitionManager mgr = new MicroserviceDefinitionManager();
        mgr.init(loader.getConfigModels());

        for (MicroserviceDefinition microserviceDefinition : mgr.getDefinitionMap().values()) {
            addMicroservice(microserviceDefinition);
        }
    }

    public Microservice addMicroservice(String appId, String microserviceName) {
        MicroserviceDefinition microserviceDefinition = MicroserviceDefinition.create(appId, microserviceName);
        return addMicroservice(microserviceDefinition);
    }

    public Microservice addMicroservice(MicroserviceDefinition microserviceDefinition) {
        Configuration configuration = microserviceDefinition.getConfiguration();
        Microservice microservice = createMicroserviceFromDefinition(configuration);
        microservice.setIntance(createMicroserviceInstance(configuration));

        if (appId == null) {
            appId = microservice.getAppId();
        } else {
            if (!appId.equals(microservice.getAppId())) {
                throw new IllegalArgumentException(
                        String.format("Not allowed multiple appId in one process, but have %s and %s.",
                                appId,
                                microservice.getAppId()));
            }
        }
        nameMap.put(microservice.getServiceName(), microservice);
        if (!StringUtils.isEmpty(microserviceDefinition.getRootPath())) {
            rootMap.put(microserviceDefinition.getRootPath(), microservice);
        }

        defaultMicroservice = microservice;
        return microservice;
    }

    private MicroserviceInstance createMicroserviceInstance(Configuration configuration) {
        MicroserviceInstance microserviceInstance = new MicroserviceInstance();
        microserviceInstance.setStage(DefinitionConst.defaultStage);
        Map<String, String> propertiesMap = InstancePropertiesLoader.INSTANCE.loadProperties(configuration);
        microserviceInstance.setProperties(propertiesMap);

        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setMode(HealthCheckMode.HEARTBEAT);
        microserviceInstance.setHealthCheck(healthCheck);

        return microserviceInstance;
    }

    private Microservice createMicroserviceFromDefinition(Configuration configuration) {
        Microservice microservice = new Microservice();
        microservice.setServiceName(configuration.getString(DefinitionConst.qulifiedServiceNameKey, null));
        microservice.setAppId(configuration.getString(DefinitionConst.appIdKey));
        microservice.setVersion(configuration.getString(DefinitionConst.qulifiedServiceVersionKey,
                DefinitionConst.defaultVersion));
        microservice.setDescription(configuration.getString(DefinitionConst.qulifiedServiceDescKey, ""));
        microservice.setLevel(configuration.getString(DefinitionConst.qulifiedServiceRoleKey, "FRONT"));

        Map<String, String> propertiesMap = MicroservicePropertiesLoader.INSTANCE.loadProperties(configuration);
        microservice.setProperties(propertiesMap);

        // set alias name when allow cross app
        if (allowCrossApp(propertiesMap)) {
            microservice.setAlias(Microservice.generateAbsoluteMicroserviceName(microservice.getAppId(),
                    microservice.getServiceName()));
        }

        return microservice;
    }

    protected boolean allowCrossApp(Map<String, String> propertiesMap) {
        return Boolean.valueOf(propertiesMap.get(DefinitionConst.allowCrossAppKey));
    }

    public Collection<Microservice> getMicroservices() {
        return nameMap.values();
    }

    // one project/jar can has one microservice.yaml
    // all producer class in this project/jar belongs to the microservice
    public Microservice findMicroservice(Class<?> cls) {
        // for compatible
        if (!isMultipleMicroservice()) {
            return defaultMicroservice;
        }

        String rootPath = FileUtils.findRootPath(cls);
        return rootMap.get(rootPath);
    }

    public Microservice findMicroservice(String microserviceName) {
        return nameMap.get(microserviceName);
    }

    public boolean isMultipleMicroservice() {
        return nameMap.size() > 1;
    }

    public Microservice getDefaultMicroservice() {
        if (isMultipleMicroservice()) {
            throw new IllegalArgumentException("there are multiple microservices, can not use default microservice");
        }
        return defaultMicroservice;
    }

    public Microservice getDefaultMicroserviceForce() {
        return defaultMicroservice;
    }

    public MicroserviceInstance getDefaultMicroserviceInstance() {
        if (isMultipleMicroservice()) {
            throw new IllegalArgumentException(
                    "there are multiple microservices, can not use default microservice instance");
        }
        return defaultMicroservice.getIntance();
    }

    public Microservice ensureFindMicroservice(String microserviceName) {
        Microservice microservice = nameMap.get(microserviceName);
        if (microservice == null) {
            throw new IllegalArgumentException(String.format("microservice %s is not exist.", microserviceName));
        }
        return microservice;
    }
}
