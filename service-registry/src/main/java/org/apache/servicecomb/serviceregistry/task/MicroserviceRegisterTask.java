/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.serviceregistry.task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.foundation.common.base.ServiceCombConstants;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemaResponse;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.client.http.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.lang3.StringUtils;

public class MicroserviceRegisterTask extends AbstractRegisterTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceRegisterTask.class);

  private boolean schemaIdSetMatch;

  public MicroserviceRegisterTask(EventBus eventBus, ServiceRegistryClient srClient, Microservice microservice) {
    super(eventBus, srClient, microservice);
    this.taskStatus = TaskStatus.READY;
  }

  public boolean isSchemaIdSetMatch() {
    return schemaIdSetMatch;
  }

  @Subscribe
  public void onMicroserviceInstanceHeartbeatTask(MicroserviceInstanceHeartbeatTask task) {
    if (task.getHeartbeatResult() != HeartbeatResult.SUCCESS && isSameMicroservice(task.getMicroservice())) {
      LOGGER.info("read MicroserviceInstanceHeartbeatTask status is {}", task.taskStatus);
      this.taskStatus = TaskStatus.READY;
      this.registered = false;
    }
  }

  @Subscribe
  public void onInstanceRegistryFailed(MicroserviceInstanceRegisterTask task) {
    if (task.taskStatus != TaskStatus.FINISHED) {
      LOGGER.info("read MicroserviceInstanceRegisterTask status is {}", task.taskStatus);
      this.taskStatus = TaskStatus.READY;
      this.registered = false;
    }
  }

  @Override
  protected boolean doRegister() {
    LOGGER.info("running microservice register task.");
    String serviceId = srClient.getMicroserviceId(microservice.getAppId(),
        microservice.getServiceName(),
        microservice.getVersion(),
        microservice.getEnvironment());
    if (!StringUtils.isEmpty(serviceId)) {
      // This microservice has been registered, so we just use the serviceId gotten from service center
      microservice.setServiceId(serviceId);
      LOGGER.info(
          "Microservice exists in service center, no need to register. id=[{}] appId=[{}], name=[{}], version=[{}], env=[{}]",
          serviceId,
          microservice.getAppId(),
          microservice.getServiceName(),
          microservice.getVersion(),
          microservice.getEnvironment());

      if (!checkSchemaIdSet()) {
        return false;
      }
    } else {
      serviceId = srClient.registerMicroservice(microservice);
      if (StringUtils.isEmpty(serviceId)) {
        LOGGER.error(
            "Registry microservice failed. appId=[{}], name=[{}], version=[{}], env=[{}]",
            microservice.getAppId(),
            microservice.getServiceName(),
            microservice.getVersion(),
            microservice.getEnvironment());
        return false;
      }

      // In re-register microservice case, the old instanceId should not be cached
      microservice.getInstance().setInstanceId(null);

      LOGGER.info(
          "Registry Microservice successfully. id=[{}] appId=[{}], name=[{}], version=[{}], schemaIds={}, env=[{}]",
          serviceId,
          microservice.getAppId(),
          microservice.getServiceName(),
          microservice.getVersion(),
          microservice.getSchemas(),
          microservice.getEnvironment());
    }

    microservice.setServiceId(serviceId);
    microservice.getInstance().setServiceId(microservice.getServiceId());

    return registerSchemas();
  }

  private boolean checkSchemaIdSet() {
    Microservice existMicroservice = srClient.getMicroservice(microservice.getServiceId());
    if (existMicroservice == null) {
      LOGGER.error("Error to get microservice from service center when check schema set");
      return false;
    }
    Set<String> existSchemas = new HashSet<>(existMicroservice.getSchemas());
    Set<String> localSchemas = new HashSet<>(microservice.getSchemas());
    schemaIdSetMatch = existSchemas.equals(localSchemas);

    if (!schemaIdSetMatch) {
      LOGGER.warn(
          "SchemaIds is different between local and service center. "
              + "serviceId=[{}] appId=[{}], name=[{}], version=[{}], env=[{}], local schemaIds={}, service center schemaIds={}",
          microservice.getServiceId(),
          microservice.getAppId(),
          microservice.getServiceName(),
          microservice.getVersion(),
          microservice.getEnvironment(),
          localSchemas,
          existSchemas);
      return true;
    }

    LOGGER.info(
        "SchemaIds are equals to service center. serviceId=[{}], appId=[{}], name=[{}], version=[{}], env=[{}], schemaIds={}",
        microservice.getServiceId(),
        microservice.getAppId(),
        microservice.getServiceName(),
        microservice.getVersion(),
        microservice.getEnvironment(),
        localSchemas);
    return true;
  }

  private boolean registerSchemas() {
    Holder<List<GetSchemaResponse>> scSchemaHolder = srClient.getSchemas(microservice.getServiceId());
    if (Status.OK.getStatusCode() != scSchemaHolder.getStatusCode()) {
      LOGGER.error("failed to get schemas from service center, statusCode = [{}]", scSchemaHolder.getStatusCode());
      return false;
    }

    Map<String, GetSchemaResponse> scSchemaMap = convertScSchemaMap(scSchemaHolder);
    // CHECK: local > sc, local != sc
    for (Entry<String, String> localSchemaEntry : microservice.getSchemaMap().entrySet()) {
      if (!registerSchema(scSchemaMap, localSchemaEntry)) {
        return false;
      }
    }

    // CHECK: local < sc
    checkRemainingSchema(scSchemaMap);

    schemaIdSetMatch = true;
    return true;
  }

  /**
   * Check whether a local schema is equal to a sc schema.
   * @return true if the local schema is equal to a sc schema, or be registered to sc successfully;
   * false if schema is registered to sc but failed.
   * @throws IllegalStateException The environment is not modifiable, and the local schema is different from sc schema
   * or not exist in sc.
   */
  private boolean registerSchema(Map<String, GetSchemaResponse> scSchemaMap,
      Entry<String, String> localSchemaEntry) {
    GetSchemaResponse scSchema = scSchemaMap.get(localSchemaEntry.getKey());

    boolean onlineSchemaExists = scSchema != null;
    LOGGER.info("schemaId [{}] exists [{}], summary exists [{}]",
        localSchemaEntry.getKey(),
        onlineSchemaExists,
        scSchema != null && scSchema.getSummary() != null);
    if (!onlineSchemaExists) {
      // local > sc
      return registerNewSchema(localSchemaEntry);
    }

    scSchemaMap.remove(localSchemaEntry.getKey());

    // local != sc
    return compareAndReRegisterSchema(localSchemaEntry, scSchema);
  }

  /**
   * Try to register a new schema to service center, or throw exception if cannot register.
   * @param localSchemaEntry local schema to be registered.
   * @return whether local schema is registered successfully.
   * @throws IllegalStateException The environment is unmodifiable.
   */
  private boolean registerNewSchema(Entry<String, String> localSchemaEntry) {
    // The ids of schemas are contained by microservice registry request, which means once a microservice
    // is registered in the service center, the schemas that it contains are determined.
    // If we get a microservice but cannot find the given schemaId in it's schemaId list, this means that
    // the schemas of this microservice has been changed, and we should decide whether to register this new
    // schema according to it's environment configuration.
    if (onlineSchemaIsModifiable()) {
      return registerSingleSchema(localSchemaEntry.getKey(), localSchemaEntry.getValue());
    }

    throw new IllegalStateException(
        "There is a schema only existing in local microservice: [" + localSchemaEntry.getKey()
            + "], which means there are interfaces changed. "
            + "You need to increment microservice version before deploying, "
            + "or you can configure service_description.environment="
            + ServiceCombConstants.DEVELOPMENT_SERVICECOMB_ENV
            + " to work in development environment and ignore this error");
  }

  /**
   * Compare schema summary and determine whether to re-register schema or throw exception.
   * @param localSchemaEntry local schema
   * @param scSchema schema in service center
   * @return true if the two copies of schema are the same, or local schema is re-registered successfully,
   * false if the local schema is re-registered to service center but failed.
   * @throws IllegalStateException The two copies of schema are different and the environment is not modifiable.
   */
  private boolean compareAndReRegisterSchema(Entry<String, String> localSchemaEntry, GetSchemaResponse scSchema) {
    String scSchemaSummary = getScSchemaSummary(scSchema);

    if (null == scSchemaSummary) {
      // cannot get scSchemaSummary, which means there is no schema content in sc, register schema directly
      return registerSingleSchema(localSchemaEntry.getKey(), localSchemaEntry.getValue());
    }

    String localSchemaSummary = RegistryUtils.calcSchemaSummary(localSchemaEntry.getValue());
    if (!localSchemaSummary.equals(scSchemaSummary)) {
      if (onlineSchemaIsModifiable()) {
        LOGGER.info(
            "schema[{}]'s content is changed and the current environment is [{}], so re-register it!",
            localSchemaEntry.getKey(),
            ServiceCombConstants.DEVELOPMENT_SERVICECOMB_ENV);
        return registerSingleSchema(localSchemaEntry.getKey(), localSchemaEntry.getValue());
      }

      //if local schema and service center schema is different then print the both schemas and print difference in local schema.
      String scSchemaContent = srClient.getSchema(microservice.getServiceId(), scSchema.getSchemaId());
      String localSchemaContent = localSchemaEntry.getValue();

      LOGGER.warn(
          "service center schema and local schema both are different:\n service center schema:\n[{}\n local schema:\n[{}]",
          scSchemaContent,
          localSchemaContent);
      String diffStringLocal = StringUtils.difference(scSchemaContent, localSchemaContent);
      if (diffStringLocal.equals("")) {
        LOGGER.warn("Some APIs are deleted in local schema which are present in service center schema \n");
      } else {
        LOGGER.warn("The difference in local schema:\n[{}]", diffStringLocal);
      }

      // env is not development, throw an exception and break the init procedure
      throw new IllegalStateException(
          "The schema(id=[" + localSchemaEntry.getKey()
              + "]) content held by this instance and the service center is different. "
              + "You need to increment microservice version before deploying. "
              + "Or you can configure service_description.environment="
              + ServiceCombConstants.DEVELOPMENT_SERVICECOMB_ENV
              + " to work in development environment and ignore this error");
    }

    // summaries are the same
    return true;
  }

  /**
   * Try to get or calculate scSchema summary.
   * @return summary of scSchema,
   * or null if there is no schema content in service center
   */
  private String getScSchemaSummary(GetSchemaResponse scSchema) {
    String scSchemaSummary = scSchema.getSummary();
    if (null != scSchemaSummary) {
      return scSchemaSummary;
    }

    // if there is no online summery, query online schema content directly and calculate summary
    String onlineSchemaContent = srClient.getSchema(microservice.getServiceId(), scSchema.getSchemaId());
    if (null != onlineSchemaContent) {
      scSchemaSummary = RegistryUtils.calcSchemaSummary(onlineSchemaContent);
    }

    return scSchemaSummary;
  }

  /**
   * Check whether there are schemas remaining in service center but not exist in local microservice.
   * @throws IllegalStateException There are schemas only existing in service center, and the environment is unmodifiable.
   */
  private void checkRemainingSchema(Map<String, GetSchemaResponse> scSchemaMap) {
    if (!scSchemaMap.isEmpty()) {
      // there are some schemas only exist in service center
      if (!onlineSchemaIsModifiable()) {
        // env is not development, throw an exception and break the init procedure
        throw new IllegalStateException("There are schemas only existing in service center: " + scSchemaMap.keySet()
            + ", which means there are interfaces changed. "
            + "You need to increment microservice version before deploying, "
            + "or if service_description.environment="
            + ServiceCombConstants.DEVELOPMENT_SERVICECOMB_ENV
            + ", you can delete microservice information in service center and restart this instance.");
      }

      // Currently nothing to do but print a warning
      LOGGER.warn("There are schemas only existing in service center: {}, which means there are interfaces changed. "
          + "It's recommended to increment microservice version before deploying.",
          scSchemaMap.keySet());
      LOGGER.warn("ATTENTION: The schemas in new version are less than the old version, "
          + "which may cause compatibility problems.");
    }
  }

  private boolean onlineSchemaIsModifiable() {
    return ServiceCombConstants.DEVELOPMENT_SERVICECOMB_ENV.equalsIgnoreCase(microservice.getEnvironment());
  }

  /**
   * Register a schema directly.
   * @return true if register success, otherwise false
   */
  private boolean registerSingleSchema(String schemaId, String content) {
    return srClient.registerSchema(microservice.getServiceId(), schemaId, content);
  }

  private Map<String, GetSchemaResponse> convertScSchemaMap(Holder<List<GetSchemaResponse>> scSchemaHolder) {
    Map<String, GetSchemaResponse> scSchemaMap = new HashMap<>();
    List<GetSchemaResponse> scSchemaList = scSchemaHolder.getValue();
    if (null == scSchemaList) {
      return scSchemaMap;
    }

    for (GetSchemaResponse scSchema : scSchemaList) {
      scSchemaMap.put(scSchema.getSchemaId(), scSchema);
    }

    return scSchemaMap;
  }
}
