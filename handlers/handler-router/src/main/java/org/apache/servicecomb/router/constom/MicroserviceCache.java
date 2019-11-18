package org.apache.servicecomb.router.constom;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;

public final class MicroserviceCache {

  private static MicroserviceCache instance = new MicroserviceCache();
  private Map<String, Microservice> services = new HashMap<>();

  private MicroserviceCache() {
  }

  public static MicroserviceCache getInstance() {
    return instance;
  }

  public Microservice getService(String serviceId) {
    Microservice micorservice = services.computeIfAbsent(serviceId, (k) -> {
      return RegistryUtils.getMicroservice(serviceId);
    });
    return micorservice;
  }
}
