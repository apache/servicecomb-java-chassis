package org.apache.servicecomb.darklaunch;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.RegistryUtils;

public final class MicroserviceCache {
  private static MicroserviceCache instance = new MicroserviceCache();

  private Map<String, Microservice> services = new HashMap<>();

  private MicroserviceCache() {
  }

  public static MicroserviceCache getInstance() {
    return instance;
  }

  public Microservice getService(String serviceId) {
    Microservice microservice = services.computeIfAbsent(serviceId, (k) -> {
      return RegistryUtils.getMicroservice(serviceId);
    });
    return microservice;
  }
}
