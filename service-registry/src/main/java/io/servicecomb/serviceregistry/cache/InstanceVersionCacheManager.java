package io.servicecomb.serviceregistry.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.servicecomb.serviceregistry.ServiceRegistry;
import io.servicecomb.serviceregistry.api.Const;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;

public class InstanceVersionCacheManager {
  private EventBus eventBus;

  private ServiceRegistry serviceRegistry;

  //所有service版本的Instance缓存
  // init from ALL_VERSION_RULE
  // but save by real version, eg: 0.0.1/0.0.2/0.0.3......
  protected Map<String, Map<String, Map<String, MicroserviceInstance>>> cacheAllMap = new ConcurrentHashMap<>();

  private static final String ALL_VERSION_RULE = "0+";

  private static final String MICROSERVICE_DEFAULT_VERSION = "microservice.default.version";

  private static final Object LOCKOBJECT = new Object();

  public InstanceVersionCacheManager(EventBus eventBus, ServiceRegistry serviceRegistry) {
    this.eventBus = eventBus;
    this.serviceRegistry = serviceRegistry;

    this.eventBus.register(this);
  }

  private static String getKey(String appId, String microserviceName) {
    if (microserviceName.contains(Const.APP_SERVICE_SEPARATOR)) {
      return microserviceName.replace(Const.APP_SERVICE_SEPARATOR, "/");
    }

    StringBuilder sb = new StringBuilder(appId.length() + microserviceName.length() + 1);
    sb.append(appId).append("/").append(microserviceName);
    return sb.toString();
  }

  private Map<String, MicroserviceInstance> create(String appId, String microserviceName,
      String microserviceVersionRule) {
    List<MicroserviceInstance> instances =
        serviceRegistry.findServiceInstance(appId, microserviceName, microserviceVersionRule);
    if (instances == null) {
      return null;
    }

    Map<String, MicroserviceInstance> instMap = new HashMap<>();
    for (MicroserviceInstance instance : instances) {
      instMap.put(instance.getInstanceId(), instance);
    }
    return instMap;
  }

  public Map<String, MicroserviceInstance> getOrCreateAllMap(String appId, String microserviceName,
      String microserviceVersion) {
    Map<String, Map<String, MicroserviceInstance>> cache = getOrCreateAllMap(appId, microserviceName);
    return cache.get(microserviceVersion);
  }

  public Map<String, Map<String, MicroserviceInstance>> getOrCreateAllMap(String appId, String microserviceName) {
    String key = getKey(appId, microserviceName);
    Map<String, Map<String, MicroserviceInstance>> cache = cacheAllMap.get(key);
    if (cache == null) {
      synchronized (LOCKOBJECT) {
        cache = cacheAllMap.get(key);
        if (cache == null) {
          Map<String, MicroserviceInstance> cacheAllInstance =
              create(appId, microserviceName, ALL_VERSION_RULE);
          cache = createCacheVersionMap(cacheAllInstance);
          cacheAllMap.put(key, cache);
        }
      }
    }
    return cache;
  }

  private Map<String, Map<String, MicroserviceInstance>> createCacheVersionMap(
      Map<String, MicroserviceInstance> cacheAllInstance) {
    Map<String, Map<String, MicroserviceInstance>> cacheVersionMap =
        new HashMap<String, Map<String, MicroserviceInstance>>();
    for (Map.Entry<String, MicroserviceInstance> ins : cacheAllInstance.entrySet()) {
      String microserviceId = ins.getValue().getServiceId();
      Microservice microservice = serviceRegistry.getRemoteMicroservice(microserviceId);
      String version = microservice.getVersion();
      if (version == null || "".equals(version)) {
        version = MICROSERVICE_DEFAULT_VERSION;
      }
      if (cacheVersionMap.get(version) == null) {
        Map<String, MicroserviceInstance> newInsMap = new HashMap<String, MicroserviceInstance>();
        newInsMap.put(ins.getKey(), ins.getValue());
        cacheVersionMap.put(version, newInsMap);
      } else {
        Map<String, MicroserviceInstance> insMap = cacheVersionMap.get(version);
        insMap.put(ins.getKey(), ins.getValue());
      }
    }

    return cacheVersionMap;
  }

  @Subscribe
  public void onInstanceUpdate(MicroserviceInstanceChangedEvent changedEvent) {
    String appId = changedEvent.getKey().getAppId();
    String microserviceName = changedEvent.getKey().getServiceName();
    String version = changedEvent.getKey().getVersion();
    String key = getKey(appId, microserviceName);

    synchronized (LOCKOBJECT) {

      Map<String, Map<String, MicroserviceInstance>> allCache = cacheAllMap.get(key);

      switch (changedEvent.getAction()) {
        case CREATE:
        case UPDATE:

          if (allCache != null) {
            MicroserviceInstance newIns = changedEvent.getInstance();
            String instanceId = newIns.getInstanceId();
            if (allCache.get(version) == null) {
              Map<String, MicroserviceInstance> newInsMap = new HashMap<String, MicroserviceInstance>();
              newInsMap.put(instanceId, newIns);
              allCache.put(version, newInsMap);
            } else {
              Map<String, MicroserviceInstance> insMap = allCache.get(version);
              insMap.put(instanceId, newIns);
            }
          }
          break;

        case EXPIRE:
          if (allCache != null) {
            if (allCache.get(version) != null) {
              Map<String, MicroserviceInstance> insMap = allCache.get(version);
              insMap.clear();
            }
          }
          break;

        case DELETE:
          if (allCache != null) {
            String instanceId = changedEvent.getInstance().getInstanceId();
            if (allCache.get(version) != null) {
              Map<String, MicroserviceInstance> insMap = allCache.get(version);
              insMap.remove(instanceId);
            }
          }
          break;
        default:
          return;
      }
    }
  }

  public void cleanUp() {
    synchronized (LOCKOBJECT) {
      cacheAllMap.clear();
    }
  }
}
