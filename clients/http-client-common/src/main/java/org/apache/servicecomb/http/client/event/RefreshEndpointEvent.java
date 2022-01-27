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

package org.apache.servicecomb.http.client.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefreshEndpointEvent {

  public static final String SERVICE_CENTER_NAME = "SERVICECENTER";

  public static final String KIE_NAME = "KIE";

  public static final String CONFIG_CENTER_NAME = "CseConfigCenter";

  public static final String CSE_MONITORING_NAME = "CseMonitoring";

  private static final String SAME_ZONE = "sameZone";

  private static final String SAME_REGION = "sameRegion";

  private Map<String, List<String>> zoneAndRegion = new HashMap<>();

  private String name;

  public RefreshEndpointEvent(Map<String, List<String>> zoneAndRegion, String name) {
    this.zoneAndRegion = zoneAndRegion;
    this.name = name;
  }

  public List<String> getSameZone() {
    if (zoneAndRegion.get(SAME_ZONE).isEmpty()) {
      return new ArrayList<>();
    }
    return zoneAndRegion.get(SAME_ZONE);
  }

  public List<String> getSameRegion() {
    if (zoneAndRegion.get(SAME_REGION).isEmpty()) {
      return new ArrayList<>();
    }
    return zoneAndRegion.get(SAME_REGION);
  }

  public Map<String, List<String>> getZoneAndRegion() {
    return zoneAndRegion;
  }

  public void setZoneAndRegion(Map<String, List<String>> zoneAndRegion) {
    this.zoneAndRegion = zoneAndRegion;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
