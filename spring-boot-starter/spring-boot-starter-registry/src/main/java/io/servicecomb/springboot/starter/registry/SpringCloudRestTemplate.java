/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.springboot.starter.registry;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import io.servicecomb.core.Handler;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.foundation.common.net.IpPort;
import io.servicecomb.foundation.common.net.NetUtils;
import io.servicecomb.foundation.ssl.SSLCustom;
import io.servicecomb.foundation.ssl.SSLManager;
import io.servicecomb.foundation.ssl.SSLOption;
import io.servicecomb.foundation.ssl.SSLOptionFactory;
import io.servicecomb.loadbalance.LoadbalanceHandler;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.swagger.invocation.AsyncResponse;

/**
* Initialize the spring cloud rest template 
*/
public class SpringCloudRestTemplate extends RestTemplate {
  private static final String SSL_KEY = "springcloud.consumer";

  private static final Logger LOG = LoggerFactory.getLogger(SpringCloudRestTemplate.class);

  private URI newURI;

  Map<String, LoadbalanceHandler> handlersMap = new HashMap<>();

  public SpringCloudRestTemplate() {
    super();
    SSLOptionFactory factory = SSLOptionFactory.createSSLOptionFactory(SSL_KEY, null);
    SSLOption option;
    if (factory == null) {
      option = SSLOption.buildFromYaml(SSL_KEY, null);
    } else {
      option = factory.createSSLOption();
    }
    SSLCustom custom = SSLCustom.createSSLCustom(option.getSslCustomClass());
    SSLContext sslContext = SSLManager.createSSLContext(option, custom);

    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    HostnameVerifier verifier;
    if (option.isCheckCNHost()) {
      verifier = new DefaultHostnameVerifier();
    } else {
      verifier = new HostnameVerifier() {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
          return true;
        }
      };
    }
    HttpClient httpClient = HttpClients.custom()
        .setSSLContext(sslContext)
        .setSSLHostnameVerifier(verifier)
        .build();
    requestFactory.setHttpClient(httpClient);
    setRequestFactory(requestFactory);
  }

  protected ClientHttpRequest createRequest(URI uri, HttpMethod method) throws IOException {
    String serviceName = uri.getAuthority();

    Invocation invocation = new Invocation(new ReferenceConfig(), new OperationMeta(), null) {
      public List<Handler> getHandlerChain() {
        return null;
      }

      public void next(AsyncResponse asyncResp) throws Exception {
        asyncResp.success("DONE");
      }

      public String getAppId() {
        return RegistryUtils.getAppId();
      }

      public String getMicroserviceName() {
        return serviceName;
      }

      public String getMicroserviceVersionRule() {
        return "0.0.0+";
      }

      public String getRealTransportName() {
        return "rest";
      }

      public String getSchemaId() {
        return "default";
      }

      public String getOperationName() {
        return "default";
      }
    };

    LoadbalanceHandler lb = handlersMap.computeIfAbsent(serviceName, key -> {
      return new LoadbalanceHandler();
    });

    try {
      lb.handle(invocation, response -> {
        try {
          IpPort ipPort = NetUtils.parseIpPortFromURI(invocation.getEndpoint().getEndpoint());
          newURI = new URI(uri.getScheme(), uri.getUserInfo(), ipPort.getHostOrIp(), ipPort.getPort(),
              uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
          LOG.error("URI syntax error during create request.", e);
        }
      });
    } catch (Exception e) {
      LOG.error("Handler error during create request.", e);
    }

    return super.createRequest(newURI, method);

  }
}
