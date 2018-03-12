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
package org.apache.servicecomb.serviceregistry.api.registry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.Versions;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrameworkVersions {
  private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkVersions.class);
  private static final ServiceLoader<Versions> frameworkVersions = ServiceLoader.load(Versions.class);

  public static String allVersions() {
    ServiceRegistryClient client = RegistryUtils.getServiceRegistryClient();
    ServiceCenterInfo serviceCenterInfo = client.getServiceCenterInfo();
    if (serviceCenterInfo == null) {
      LOGGER.error("query servicecenter version info failed.");
    }
    String scVersion = serviceCenterInfo.getVersion();
    //old scVersion which earlier than 1.0.0 not report frameworkVersion, e.g. 0.5.0
    String oldScVersion = "0.[0-5].0+";
    if (scVersion.matches(oldScVersion)) {
      return "";
    }

    Map<String, String> versions = new HashMap<>();
    Entry<String, String> entry;
    StringBuffer sb = new StringBuffer();

    frameworkVersions.forEach(version -> versions.putAll(version.loadVersion()));
    for (Iterator<Entry<String, String>> iterator = versions.entrySet().iterator(); iterator.hasNext();) {
      entry = (Entry<String, String>) iterator.next();
      sb.append(entry.getKey()).append(":").append(entry.getValue())
        .append(iterator.hasNext() ? ";" : "");
    }
    return sb.toString();
  }
}
