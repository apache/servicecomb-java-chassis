package org.apache.servicecomb.router.constom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.loadbalance.ServerListFilterExt;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.json.Json;
import org.apache.servicecomb.router.RouterFilter;
import org.apache.servicecomb.router.distribute.RouterDistributor;

public class CanaryServerListFilter implements ServerListFilterExt {

  private static final String ENABLE = "servicecomb.release_way";

  RouterDistributor distributer = new ServiceCombCanaryDistributer();

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance().getStringProperty(ENABLE, "").get()
        .equals("canary");
  }

  @Override
  public List<ServiceCombServer> getFilteredListOfServers(List<ServiceCombServer> list,
      Invocation invocation) {
    String targetServiceName = invocation.getMicroserviceName();
    Map<String, String> headers = addHeaders(invocation);
    return RouterFilter
        .getFilteredListOfServers(list, targetServiceName, headers,
            distributer);
  }

  private Map<String, String> addHeaders(Invocation invocation) {
    Map<String, String> headers = new HashMap<>();
    if (invocation.getContext("canary_context") != null) {
      Map<String, String> canaryContext = Json
          .decodeValue(invocation.getContext("canary_context"), Map.class);
      headers.putAll(canaryContext);
    }
    for (int i = 0; i < invocation.getArgs().length; i++) {
      if (invocation.getOperationMeta().getParamName(i) != null &&
          invocation.getArgs()[i] != null) {
        headers
            .put(invocation.getOperationMeta().getParamName(i), invocation.getArgs()[i].toString());
      }
    }
    headers.putAll(invocation.getContext());
    return headers;
  }
}
