package org.apache.servicecomb.http.client.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefreshEndpointEvent {

  private static final String SAME_ZONE = "sameZone";

  private static final String SAME_REGION = "sameRegion";

  private Map<String, List<String>> zoneAndRegion;

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
