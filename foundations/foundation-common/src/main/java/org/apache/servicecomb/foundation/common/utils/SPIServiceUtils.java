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

package org.apache.servicecomb.foundation.common.utils;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

/**
 * SPI Service utils
 *
 *
 */
public final class SPIServiceUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SPIServiceUtils.class);

  // load one service, maybe trigger load another service
  // computeIfAbsent can not support this feature
  // so use double check
  private static final Object LOCK = new Object();

  private static final Map<Class<?>, List<Object>> cache = new ConcurrentHashMap<>();

  private SPIServiceUtils() {

  }

  /**
   * no cache, return new instances every time.
   */
  public static <T> List<T> loadSortedService(Class<T> serviceType) {
    List<Entry<Integer, T>> serviceEntries = new ArrayList<>();
    ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceType);
    serviceLoader.forEach(service -> {
      int serviceOrder = 0;
      Method getOrder = ReflectionUtils.findMethod(service.getClass(), "getOrder");
      if (getOrder != null) {
        serviceOrder = (int) ReflectionUtils.invokeMethod(getOrder, service);
      }

      Entry<Integer, T> entry = new SimpleEntry<>(serviceOrder, service);
      serviceEntries.add(entry);
    });

    List<T> services = serviceEntries.stream()
        .sorted(Comparator.comparingInt(Entry::getKey))
        .map(Entry::getValue)
        .collect(Collectors.toList());

    LOGGER.info("Found SPI service {}, count={}.", serviceType.getName(), services.size());
    for (int idx = 0; idx < services.size(); idx++) {
      T service = services.get(idx);
      LOGGER.info("  {}. {}.", idx, service.getClass().getName());
    }

    return services;
  }

  @SuppressWarnings("unchecked")
  public static <T> List<T> getOrLoadSortedService(Class<T> serviceType) {
    List<Object> services = cache.get(serviceType);
    if (services == null) {
      synchronized (LOCK) {
        services = cache.get(serviceType);
        if (services == null) {
          services = (List<Object>) loadSortedService(serviceType);
          cache.put(serviceType, services);
        }
      }
    }

    return (List<T>) services;
  }

  /**
   * get target service.if target services are array,only random access to a service.
   */
  public static <T> T getTargetService(Class<T> serviceType) {
    List<T> services = getOrLoadSortedService(serviceType);
    if (services.isEmpty()) {
      LOGGER.info("Can not find SPI service for {}", serviceType.getName());
      return null;
    }

    return services.get(0);
  }

  public static <T> List<T> getAllService(Class<T> serviceType) {
    return getOrLoadSortedService(serviceType);
  }

  public static <T> List<T> getSortedService(Class<T> serviceType) {
    return getOrLoadSortedService(serviceType);
  }

  public static <T> T getPriorityHighestService(Class<T> serviceType) {
    List<T> services = getOrLoadSortedService(serviceType);
    if (services.isEmpty()) {
      LOGGER.info("Can not find SPI service for {}", serviceType.getName());
      return null;
    }

    return services.get(0);
  }

  @SuppressWarnings("unchecked")
  public static <T, IMPL> IMPL getTargetService(Class<T> serviceType, Class<IMPL> implType) {
    List<T> services = getOrLoadSortedService(serviceType);
    return (IMPL) services
        .stream()
        .filter(service -> service.getClass().equals(implType))
        .findFirst()
        .orElse(null);
  }
}
