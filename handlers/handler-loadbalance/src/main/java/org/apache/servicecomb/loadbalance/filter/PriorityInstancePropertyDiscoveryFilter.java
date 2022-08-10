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

package org.apache.servicecomb.loadbalance.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.discovery.AbstractDiscoveryFilter;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

/**
 * Instance property with priority filter
 */
public class PriorityInstancePropertyDiscoveryFilter extends AbstractDiscoveryFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(PriorityInstancePropertyDiscoveryFilter.class);

  private static final String ALL_INSTANCE = "allInstance";

  private String propertyKey;

  @Override
  protected void init(DiscoveryContext context, DiscoveryTreeNode parent) {
    propertyKey = DynamicPropertyFactory.getInstance()
        .getStringProperty("servicecomb.loadbalance.filter.priorityInstanceProperty.key", "environment")
        .get();

    // group all instance by property
    Map<String, MicroserviceInstance> instances = parent.data();
    Map<String, Map<String, MicroserviceInstance>> groupByProperty = new HashMap<>();
    for (MicroserviceInstance microserviceInstance : instances.values()) {
      String propertyValue = new PriorityInstanceProperty(propertyKey, microserviceInstance).getPropertyValue();
      groupByProperty.computeIfAbsent(propertyValue, key -> new HashMap<>())
          .put(microserviceInstance.getInstanceId(), microserviceInstance);
    }
    Map<String, DiscoveryTreeNode> children = new HashMap<>();
    for (Map.Entry<String, Map<String, MicroserviceInstance>> entry : groupByProperty.entrySet()) {
      children.put(entry.getKey(),
          new DiscoveryTreeNode().subName(parent, entry.getKey()).data(entry.getValue()));
    }
    children.put(ALL_INSTANCE, new DiscoveryTreeNode().subName(parent, ALL_INSTANCE).data(instances));
    parent.children(children);
  }

  @Override
  protected String findChildName(DiscoveryContext context, DiscoveryTreeNode parent) {
    Invocation invocation = context.getInputParameters();

    // context property has precedence over instance property
    String initPropertyValue = invocation.getContext()
        .computeIfAbsent("x-" + propertyKey,
            key -> new PriorityInstanceProperty(propertyKey, RegistrationManager.INSTANCE.getMicroserviceInstance())
                .getPropertyValue());

    PriorityInstanceProperty currentProperty = context.getContextParameter(propertyKey);
    // start with initial value, then search with priority
    if (Objects.isNull(currentProperty)) {
      currentProperty = new PriorityInstanceProperty(propertyKey, initPropertyValue);
      while (!parent.children().containsKey(currentProperty.getPropertyValue())
          && currentProperty.hasChildren()) {
        currentProperty = currentProperty.child();
      }
    } else {
      if (currentProperty.hasChildren()) {
        currentProperty = currentProperty.child();
      }
    }
    LOGGER.debug("Discovery instance filter by {}", currentProperty);
    context.putContextParameter(propertyKey, currentProperty);

    // stop push filter stack if property is empty
    if (currentProperty.isEmpty()) {
      return currentProperty.getPropertyValue();
    }
    context.pushRerunFilter();
    return currentProperty.getPropertyValue();
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.loadbalance.filter.priorityInstanceProperty.enabled", false)
        .get();
  }

  @Override
  public int getOrder() {
    return new InstancePropertyDiscoveryFilter().getOrder() + 1;
  }

  static class PriorityInstanceProperty {
    private static final int MAX_LENGTH = 10000;

    private static final String SEPARATOR = ".";

    private final String propertyKey;

    private final String propertyVal;

    /**
     * Constructor
     *
     * @param key property key
     * @param value property value
     */
    public PriorityInstanceProperty(@NotNull String key, String value) {
      propertyKey = key;
      if (Objects.isNull(value)) {
        value = StringUtils.EMPTY;
      }
      if (value.length() > MAX_LENGTH) {
        throw new IllegalArgumentException("property value exceed max length");
      }
      propertyVal = value;
    }

    /**
     * Constructor
     *
     * @param key property key
     * @param microserviceInstance instance
     */
    public PriorityInstanceProperty(@NotNull String key, @NotNull MicroserviceInstance microserviceInstance) {
      this(key, Optional.ofNullable(microserviceInstance.getProperties().get(key)).orElse(StringUtils.EMPTY));
    }

    /**
     * whether property is empty
     *
     * @return result
     */
    public boolean isEmpty() {
      return StringUtils.isEmpty(propertyVal);
    }

    /**
     * does property have lower priority children
     *
     * @return result
     */
    public boolean hasChildren() {
      return StringUtils.isNotEmpty(propertyVal);
    }

    /**
     * get lower priority child
     *
     * @return result
     */
    public PriorityInstanceProperty child() {
      if (propertyVal.contains(SEPARATOR)) {
        return new PriorityInstanceProperty(propertyKey, StringUtils.substringBeforeLast(propertyVal, SEPARATOR));
      }
      return new PriorityInstanceProperty(propertyKey, StringUtils.EMPTY);
    }

    /**
     * get property value
     *
     * @return propertyVal
     */
    public String getPropertyValue() {
      return propertyVal;
    }

    @Override
    public String toString() {
      return "PriorityInstanceProperty{key=" + propertyKey + ", value=" + propertyVal + '}';
    }
  }
}
