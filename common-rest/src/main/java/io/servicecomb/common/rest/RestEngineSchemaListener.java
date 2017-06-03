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

package io.servicecomb.common.rest;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.locator.ServicePathManager;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.definition.loader.SchemaListener;

@Component
public class RestEngineSchemaListener implements SchemaListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestEngineSchemaListener.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSchemaLoaded(SchemaMeta... schemaMetas) {
        // 此时相应的ServicePathManager可能正在被使用，为避免太高的复杂度，使用copy on write逻辑
        Map<String, ServicePathManager> mgrMap = new HashMap<>();
        for (SchemaMeta schemaMeta : schemaMetas) {
            MicroserviceMeta microserviceMeta = schemaMeta.getMicroserviceMeta();
            ServicePathManager mgr = findPathManager(mgrMap, microserviceMeta);

            if (mgr.isSchemaExists(schemaMeta.getSchemaId())) {
                LOGGER.info("on schema loaded, exists schema. {}:{}",
                        schemaMeta.getMicroserviceName(),
                        schemaMeta.getSchemaId());
                continue;
            }
            LOGGER.info("on schema loaded, new schema. {}:{}",
                    schemaMeta.getMicroserviceName(),
                    schemaMeta.getSchemaId());
            mgr.addSchema(schemaMeta.getSchemaId());

            for (OperationMeta operationMeta : schemaMeta.getOperations()) {
                RestOperationMeta restOperationMeta = new RestOperationMeta();
                restOperationMeta.init(operationMeta);
                operationMeta.putExtData(RestConst.SWAGGER_REST_OPERATION, restOperationMeta);
                mgr.addResource(restOperationMeta);
            }
        }

        for (ServicePathManager mgr : mgrMap.values()) {
            // 对具有动态path operation进行排序
            mgr.sortPath();
            mgr.printService();

            mgr.saveToMicroserviceMeta();
        }
    }

    protected ServicePathManager findPathManager(Map<String, ServicePathManager> mgrMap,
            MicroserviceMeta microserviceMeta) {
        ServicePathManager mgr = mgrMap.get(microserviceMeta.getName());
        if (mgr != null) {
            return mgr;
        }

        mgr = ServicePathManager.getServicePathManager(microserviceMeta);
        if (mgr == null) {
            mgr = new ServicePathManager(microserviceMeta);
        } else {
            mgr = mgr.cloneServicePathManager();
        }
        mgrMap.put(microserviceMeta.getName(), mgr);
        return mgr;
    }
}
