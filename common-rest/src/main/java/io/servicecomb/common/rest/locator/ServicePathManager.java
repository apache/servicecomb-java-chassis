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

package io.servicecomb.common.rest.locator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.servicecomb.common.rest.definition.RestOperationComparator;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.paas.cse.core.definition.MicroserviceMeta;

/**
 * 对静态路径和动态路径的operation进行预先处理，加速operation的查询定位
 * @author   
 * @version  [版本号, 2017年1月2日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ServicePathManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServicePathManager.class);

    private static final String REST_PATH_MANAGER = "RestServicePathManager";

    protected MicroserviceMeta microserviceMeta;

    // 运行阶段,静态path,一次直接查找到目标,不必遍历查找
    // 以path为key
    protected Map<String, OperationGroup> staticPathOperations = new HashMap<>();

    // 运行阶段,以path优先级,从高到低排列的operation列表
    protected List<RestOperationMeta> dynamicPathOperationsList = new ArrayList<>();

    // 已经有哪些schemaId的path信息加进来了
    // 在producer场景中，业务before producer provider事件中将契约注册进来，此时会触发事件，携带注册范围的信息
    // 启动流程的最后阶段，同样会触发一次事件，此时是全量的信息
    // 所以，可能会重复
    protected Set<String> schemaIdSet = new HashSet<>();

    public static ServicePathManager getServicePathManager(MicroserviceMeta microserviceMeta) {
        return microserviceMeta.getExtData(REST_PATH_MANAGER);
    }

    public void saveToMicroserviceMeta() {
        microserviceMeta.putExtData(REST_PATH_MANAGER, this);
    }

    /**
     * <构造函数> [参数说明]
     */
    public ServicePathManager(MicroserviceMeta microserviceMeta) {
        this.microserviceMeta = microserviceMeta;
    }

    public MicroserviceMeta getMicroserviceMeta() {
        return microserviceMeta;
    }

    public boolean isSchemaExists(String schemaId) {
        return schemaIdSet.contains(schemaId);
    }

    public void addSchema(String schemaId) {
        schemaIdSet.add(schemaId);
    }

    public ServicePathManager cloneServicePathManager() {
        ServicePathManager mgr = new ServicePathManager(microserviceMeta);
        mgr.staticPathOperations.putAll(staticPathOperations);
        mgr.dynamicPathOperationsList.addAll(dynamicPathOperationsList);
        mgr.schemaIdSet.addAll(schemaIdSet);
        return mgr;
    }

    public OperationLocator locateOperation(String path, String httpMethod) {
        String standPath = OperationLocator.getStandardPath(path);
        OperationLocator locator = new OperationLocator();
        locator.locate(this, standPath, httpMethod);

        return locator;
    }

    public void sortPath() {
        RestOperationComparator comparator = new RestOperationComparator();
        Collections.sort(this.dynamicPathOperationsList, comparator);
    }

    public void addResource(RestOperationMeta swaggerRestOperation) {
        if (swaggerRestOperation.isAbsoluteStaticPath()) {
            // 静态path
            addStaticPathResource(swaggerRestOperation);
            return;
        }

        dynamicPathOperationsList.add(swaggerRestOperation);
    }

    protected void addStaticPathResource(RestOperationMeta operation) {
        String httpMethod = operation.getHttpMethod();
        String path = operation.getAbsolutePath();
        OperationGroup group = staticPathOperations.get(path);
        if (group == null) {
            group = new OperationGroup();
            group.register(httpMethod, operation);
            staticPathOperations.put(path, group);
            return;
        }

        if (group.findValue(httpMethod) == null) {
            group.register(httpMethod, operation);
            return;
        }

        throw new RuntimeException(
                String.format("operation with url %s, method %s is duplicated", path, httpMethod));
    }

    public Map<String, OperationGroup> getStaticPathOperationMap() {
        return staticPathOperations;
    }

    public List<RestOperationMeta> getDynamicPathOperationList() {
        return dynamicPathOperationsList;
    }

    public void printService() {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }

        doPrintService();
    }

    protected void doPrintService() {
        for (Entry<String, OperationGroup> entry : staticPathOperations.entrySet()) {
            OperationGroup operationGroup = entry.getValue();
            for (RestOperationMeta operation : operationGroup.values()) {
                LOGGER.debug(entry.getKey() + " " + operation.getHttpMethod());
            }
        }

        for (RestOperationMeta operation : getDynamicPathOperationList()) {
            LOGGER.debug(operation.getAbsolutePath() + " " + operation.getHttpMethod());
        }
    }
}
