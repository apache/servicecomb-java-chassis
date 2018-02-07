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

import org.apache.servicecomb.serviceregistry.api.Versions;

public class FrameworkVersions {
  private static final ServiceLoader<Versions> frameworkVersions = ServiceLoader.load(Versions.class);

  public static String allVersions() {
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
