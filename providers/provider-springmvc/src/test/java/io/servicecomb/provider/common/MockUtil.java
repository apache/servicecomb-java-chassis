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

package io.servicecomb.provider.common;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Map;

import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.locator.OperationLocator;
import io.servicecomb.common.rest.locator.ServicePathManager;
import io.servicecomb.core.CseContext;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.definition.SchemaUtils;
import io.servicecomb.core.definition.schema.ConsumerSchemaFactory;
import io.servicecomb.core.provider.consumer.ConsumerProviderManager;
import io.servicecomb.core.provider.consumer.InvokerUtils;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.provider.springmvc.reference.CseClientHttpRequest;
import io.servicecomb.provider.springmvc.reference.CseClientHttpResponse;
import io.servicecomb.provider.springmvc.reference.RequestMeta;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.exception.InvocationException;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import mockit.Mock;
import mockit.MockUp;

public class MockUtil {
  MicroserviceMetaManager microserviceMetaManager = new MicroserviceMetaManager();

  private static MockUtil instance = new MockUtil();

  private MockUtil() {

  }

  public static MockUtil getInstance() {
    return instance;
  }

  public void mockConsumerProviderManager() {
    ConsumerProviderManager consumerProviderManager = new MockUp<ConsumerProviderManager>() {
      @Mock
      public ReferenceConfig getReferenceConfig(String microserviceName) {
        return new ReferenceConfig(CseContext.getInstance().getConsumerSchemaFactory(), "test", "test",
            "test");
      }
    }.getMockInstance();

    CseContext.getInstance().setConsumerProviderManager(consumerProviderManager);
  }

  public void mockRegisterManager() throws InstantiationException, IllegalAccessException {

    MicroserviceMeta microserviceMeta = microserviceMetaManager.getOrCreateMicroserviceMeta("app:test");
    microserviceMeta.putExtData("RestServicePathManager", new ServicePathManager(microserviceMeta));

    ConsumerSchemaFactory consumerSchemaFactory = new MockUp<ConsumerSchemaFactory>() {
      @Mock
      public MicroserviceMeta getOrCreateConsumer(String microserviceName) {
        return microserviceMeta;
      }
    }.getMockInstance();
    CseContext.getInstance().setConsumerSchemaFactory(consumerSchemaFactory);
  }

  public void mockServicePathManager() {

    new MockUp<ServicePathManager>() {
      @Mock
      public OperationLocator locateOperation(String path, String httpMethod) {
        OperationLocator locator = new OperationLocator();
        return locator;
      }
    };
  }

  public void mockOperationLocator() {

    new MockUp<OperationLocator>() {
      @Mock
      public RestOperationMeta getOperation() {
        RestOperationMeta rom = new RestOperationMeta();
        rom.init(Mockito.mock(OperationMeta.class));

        return rom;
      }
    };
  }

  public void mockSchemaMeta() {

    new MockUp<SchemaMeta>() {
      @Mock
      private void initOperations() throws Exception {
      }
    };
  }

  public void mockBeanUtils() {

    new MockUp<BeanUtils>() {
      @Mock
      <T> T getBean(String name) {
        return null;
      }
    };
  }

  public void mockRequestMeta() {

    new MockUp<RequestMeta>() {
      @Mock
      public OperationMeta getOperationMeta() throws Exception {
        OperationMeta om = new OperationMeta();
        om.init(new SchemaMeta(null, new MicroserviceMeta("test"), null),
            this.getClass().getMethods()[0],
            "path",
            "get",
            null);

        return om;
      }
    };
  }

  public void mockSchemaUtils() {

    new MockUp<SchemaUtils>() {
      @Mock
      Class<?> getJavaInterface(String schemaId, Swagger swagger) {
        return null;
      }
    };
  }

  public void mockOperationMeta() {

    new MockUp<OperationMeta>() {
      @Mock
      private void initException(SchemaMeta schemaMeta, Operation swaggerOperation) {
      }
    };
  }

  public void mockInvokerUtils() {

    new MockUp<InvokerUtils>() {
      @Mock
      Object syncInvoke(Invocation invocation) throws InvocationException {
        return invocation;
      }
    };
  }

  public void mockReflectionUtils() {

    new MockUp<ReflectionUtils>() {
      @Mock
      Object getField(Field field, Object target) {
        Response response = Response.ok(200);
        return new CseClientHttpResponse(response);
      }
    };
  }

  public void mockCseClientHttpRequest() {

    new MockUp<CseClientHttpRequest>() {
      @Mock
      public void setRequestBody(Object requestBody) {

      }
    };
  }

  public void mockRestTemplate() {
    new MockUp<RestTemplate>() {
      @Mock
      public void put(String url, Object request, Object... urlVariables) throws RestClientException {
      }

      @Mock
      public void put(URI url, Object request) throws RestClientException {

      }

      @Mock
      public void delete(URI url) throws RestClientException {

      }

      @Mock
      public <T> T getForObject(String url, Class<T> responseType,
          Object... urlVariables) throws RestClientException {
        return null;
      }

      @Mock
      public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType,
          Object... urlVariables) throws RestClientException {
        return null;
      }

      @Mock
      public <T> T postForObject(String url, Object request, Class<T> responseType,
          Object... uriVariables) throws RestClientException {
        return null;
      }

      @Mock
      public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType,
          Object... uriVariables) throws RestClientException {
        return null;
      }

      @Mock
      public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
          HttpEntity<?> requestEntity, Class<T> responseType,
          Object... uriVariables) throws RestClientException {
        return null;
      }

      @Mock
      public void delete(String url, Object... urlVariables) throws RestClientException {

      }

      @Mock
      public void delete(String url, Map<String, ?> urlVariables) throws RestClientException {

      }

      @Mock
      public <T> T getForObject(String url, Class<T> responseType,
          Map<String, ?> urlVariables) throws RestClientException {
        return null;
      }

      @Mock
      public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType,
          Map<String, ?> urlVariables) throws RestClientException {
        return null;
      }

      @Mock
      public <T> T getForObject(URI url, Class<T> responseType) throws RestClientException {
        return null;
      }

      @Mock
      public <T> ResponseEntity<T> getForEntity(URI url, Class<T> responseType) throws RestClientException {
        return null;
      }

      @Mock
      public <T> T postForObject(String url, Object request, Class<T> responseType,
          Map<String, ?> uriVariables) throws RestClientException {
        return null;
      }

      @Mock
      public <T> T postForObject(URI url, Object request, Class<T> responseType) throws RestClientException {
        return null;
      }

      @Mock
      public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType,
          Map<String, ?> uriVariables) throws RestClientException {
        return null;
      }

      @Mock
      public <T> ResponseEntity<T> postForEntity(URI url, Object request,
          Class<T> responseType) throws RestClientException {
        return null;
      }

      @Mock
      public void put(String url, Object request, Map<String, ?> urlVariables) throws RestClientException {

      }

      @Mock
      public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
          HttpEntity<?> requestEntity, Class<T> responseType,
          Map<String, ?> uriVariables) throws RestClientException {
        return null;
      }

      @Mock
      public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
          ParameterizedTypeReference<T> responseType,
          Map<String, ?> uriVariables) throws RestClientException {
        return null;
      }

      @Mock
      public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
          ParameterizedTypeReference<T> responseType, Object... uriVariables) throws RestClientException {
        return null;
      }

      @Mock
      public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity,
          Class<T> responseType) throws RestClientException {
        return null;
      }

      @Mock
      public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity,
          ParameterizedTypeReference<T> responseType) throws RestClientException {
        return null;
      }

      @Mock
      public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity,
          Class<T> responseType) throws RestClientException {
        return null;
      }

      @Mock
      public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity,
          ParameterizedTypeReference<T> responseType) throws RestClientException {
        return null;
      }
    };
  }
}
