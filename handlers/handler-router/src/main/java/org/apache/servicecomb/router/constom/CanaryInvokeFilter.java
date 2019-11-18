package org.apache.servicecomb.router.constom;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.json.Json;

public class CanaryInvokeFilter implements HttpServerFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CanaryInvokeFilter.class);

  private static final String PASS_HEADER = "servicecomb.passheader";

  @Override
  public int getOrder() {
    return -90;
  }

  @Override
  public boolean enabled() {
    return false;
  }

  @Override
  public boolean needCacheRequest(OperationMeta operationMeta) {
    return false;
  }

  /**
   * 透传Header需要在这里实现， 因为无法预知调用链上的服务匹配所需要header, 提供两种模式 1.取到全量的header并放到context中， 2.从配置中解析并读取
   *
   * @param invocation
   * @param httpServletRequestEx
   * @return
   */
  @Override
  public Response afterReceiveRequest(Invocation invocation,
      HttpServletRequestEx httpServletRequestEx) {
    if (invocation.getContext("canary_context") != null) {
      Map<String, String> headerMap = getHeaderMap(invocation.getMicroserviceName(),
          httpServletRequestEx);
      invocation.addContext("canary_context", Json.encode(headerMap));
    }
    return null;
  }

  /**
   * 取出所用的header
   *
   * @param serviceName
   * @param httpServletRequestEx
   * @return
   */
  public Map<String, String> getHeaderMap(String serviceName,
      HttpServletRequestEx httpServletRequestEx) {
    Yaml yaml = new Yaml();
    String headerStr = DynamicPropertyFactory.getInstance().getStringProperty(PASS_HEADER, null)
        .get();
    Map<String, String> headerKeyMap = yaml.load(headerStr);
    Set<String> headerKeySet = headerKeyMap.keySet();
    Map<String, String> headerMap = new HashMap<>();
    headerKeySet.forEach(headerKey -> {
      String val = httpServletRequestEx.getHeader(headerKey);
      if (!StringUtils.isEmpty(val)) {
        headerMap.put(headerKey, httpServletRequestEx.getHeader(headerKey));
      }
    });
    return headerMap;
  }

  @Override
  public CompletableFuture<Void> beforeSendResponseAsync(Invocation invocation,
      HttpServletResponseEx responseEx) {
    return null;
  }

  @Override
  public void beforeSendResponse(Invocation invocation, HttpServletResponseEx responseEx) {
  }
}
