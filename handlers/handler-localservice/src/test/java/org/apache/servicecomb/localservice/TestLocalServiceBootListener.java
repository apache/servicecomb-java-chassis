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

package org.apache.servicecomb.localservice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.Registration;
import org.apache.servicecomb.registry.api.RegistrationInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

public class TestLocalServiceBootListener {
  private final LocalServiceBootListener listener = new LocalServiceBootListener();

  private final MockEnvironment environment = new MockEnvironment();

  @BeforeEach
  public void setUp() {
    LegacyPropertyFactory.setEnvironment(environment);
    RouterHolderDispatcher.reset();
  }

  @AfterEach
  public void tearDown() {
    LegacyPropertyFactory.setEnvironment(null);
    RouterHolderDispatcher.reset();
  }

  private BootListener.BootEvent event() {
    return new BootListener.BootEvent();
  }

  @Test
  public void disabledDoesNothing() {
    Assertions.assertFalse(LocalServiceConfig.isEnabled());
    listener.onAfterTransport(event());
    listener.onBeforeClose(event());
  }

  @Test
  public void enabledWithoutCapturedRouterDoesNothing() {
    environment.setProperty(LocalServiceConfig.KEY_ENABLED, "true");
    listener.onAfterTransport(event());
    // no exception expected: without a captured Router the listener is skipped
  }

  @Test
  public void capturedRouterLifecycle() {
    RouterHolderDispatcher holder = new RouterHolderDispatcher();
    Assertions.assertNull(RouterHolderDispatcher.getCapturedRouter());
    Assertions.assertTrue(holder.enabled() == LocalServiceConfig.isEnabled());
    Assertions.assertEquals(Integer.MIN_VALUE, holder.getOrder());
  }

  // ------------------------------------------------- local listen target parsing

  @Test
  public void wildcardAddressWithoutSslSkipsLocalListener() {
    Assertions.assertNull(LocalServiceBootListener.parseLocalListenTarget("0.0.0.0:8080"));
    Assertions.assertNull(LocalServiceBootListener.parseLocalListenTarget("[::]:8080"));
  }

  @Test
  public void wildcardAddressWithSslAlsoSkipsLocalListener() {
    // wildcard TLS binding: loopback is served by the main server with TLS;
    // the listener is skipped and the provider side auto-corrects the
    // supportLocalService declaration instead
    Assertions.assertNull(LocalServiceBootListener.parseLocalListenTarget("0.0.0.0:8080?sslEnabled=true"));
    Assertions.assertNull(LocalServiceBootListener.parseLocalListenTarget("[::]:8080?sslEnabled=true"));
  }

  // ------------------------------------------------------ loopback plain HTTP decision

  @Test
  public void loopbackPlainHttpForSpecificAddress() {
    Assertions.assertTrue(LocalServiceBootListener.isLoopbackPlainHttp("192.168.1.5:8080"));
    Assertions.assertTrue(LocalServiceBootListener.isLoopbackPlainHttp("192.168.1.5:8080?sslEnabled=true"));
  }

  @Test
  public void loopbackPlainHttpForWildcardPlainBinding() {
    Assertions.assertTrue(LocalServiceBootListener.isLoopbackPlainHttp("0.0.0.0:8080"));
    Assertions.assertTrue(LocalServiceBootListener.isLoopbackPlainHttp("[::]:8080"));
  }

  @Test
  public void loopbackNotPlainHttpForWildcardTlsBinding() {
    Assertions.assertFalse(LocalServiceBootListener.isLoopbackPlainHttp("0.0.0.0:8080?sslEnabled=true"));
    Assertions.assertFalse(LocalServiceBootListener.isLoopbackPlainHttp("[::]:8080?sslEnabled=true"));
  }

  @Test
  public void loopbackPlainHttpFalseForIllegalAddress() {
    Assertions.assertFalse(LocalServiceBootListener.isLoopbackPlainHttp(null));
    Assertions.assertFalse(LocalServiceBootListener.isLoopbackPlainHttp(""));
    Assertions.assertFalse(LocalServiceBootListener.isLoopbackPlainHttp("192.168.1.5"));
    Assertions.assertFalse(LocalServiceBootListener.isLoopbackPlainHttp("not a valid address"));
  }

  // ------------------------------------------------------ supportLocalService auto correction

  @Test
  public void declaredTrueIsCorrectedToFalseWhenLoopbackNotPlainHttp() {
    FakeRegistration registration = new FakeRegistration();
    registration.getProperties().put("supportLocalService", "true");

    listener.autoCorrectSupportLocalService(new RegistrationManager(List.of(registration)));

    Assertions.assertEquals("false", registration.getProperties().get("supportLocalService"));
  }

  @Test
  public void notDeclaredPropertyIsUntouched() {
    FakeRegistration registration = new FakeRegistration();

    listener.autoCorrectSupportLocalService(new RegistrationManager(List.of(registration)));

    Assertions.assertFalse(registration.getProperties().containsKey("supportLocalService"));
  }

  @Test
  public void declaredFalseIsUntouched() {
    FakeRegistration registration = new FakeRegistration();
    registration.getProperties().put("supportLocalService", "false");

    listener.autoCorrectSupportLocalService(new RegistrationManager(List.of(registration)));

    Assertions.assertEquals("false", registration.getProperties().get("supportLocalService"));
  }

  @Test
  public void emptyRegistrationListDoesNotThrow() {
    Assertions.assertDoesNotThrow(
        () -> listener.autoCorrectSupportLocalService(new RegistrationManager(List.of())));
  }

  @Test
  public void specificAddressNeedsLocalListener() {
    Assertions.assertEquals(8080, LocalServiceBootListener.parseLocalListenTarget("192.168.1.5:8080").getPort());
  }

  @Test
  public void addressWithQueryParameters() {
    Assertions.assertEquals(8081,
        LocalServiceBootListener.parseLocalListenTarget("192.168.1.5:8081?sslEnabled=true").getPort());
  }

  @Test
  public void addressWithoutPortIsRejected() {
    Assertions.assertNull(LocalServiceBootListener.parseLocalListenTarget("192.168.1.5"));
  }

  @Test
  public void illegalAddressIsRejected() {
    Assertions.assertNull(LocalServiceBootListener.parseLocalListenTarget("not a valid address"));
  }

  @Test
  public void nullAddressIsRejected() {
    Assertions.assertNull(LocalServiceBootListener.parseLocalListenTarget(null));
  }

  @Test
  public void loopbackIsNotWildcard() {
    Assertions.assertFalse(LocalServiceBootListener.isWildcardHost("127.0.0.1"));
    Assertions.assertTrue(LocalServiceBootListener.isWildcardHost("0.0.0.0"));
    Assertions.assertTrue(LocalServiceBootListener.isWildcardHost(""));
  }

  @Test
  public void sslQueryDetection() {
    java.net.URI uri = java.net.URI.create("http://0.0.0.0:8080?sslEnabled=true&other=x");
    Assertions.assertTrue(LocalServiceBootListener.isSslEnabled(uri));
    uri = java.net.URI.create("http://0.0.0.0:8080?sslEnabled=false");
    Assertions.assertFalse(LocalServiceBootListener.isSslEnabled(uri));
    uri = java.net.URI.create("http://0.0.0.0:8080");
    Assertions.assertFalse(LocalServiceBootListener.isSslEnabled(uri));
  }

  // ------------------------------------------------------ test doubles

  static final class FakeRegistration implements Registration<RegistrationInstance> {
    private final FakeRegistrationInstance instance = new FakeRegistrationInstance();

    Map<String, String> getProperties() {
      return instance.getProperties();
    }

    @Override
    public String name() {
      return "fake";
    }

    @Override
    public RegistrationInstance getMicroserviceInstance() {
      return instance;
    }

    @Override
    public boolean updateMicroserviceInstanceStatus(MicroserviceInstanceStatus status) {
      return true;
    }

    @Override
    public void addSchema(String schemaId, String content) {
      // no-op
    }

    @Override
    public void addEndpoint(String endpoint) {
      // no-op
    }

    @Override
    public void addProperty(String key, String value) {
      instance.getProperties().put(key, value);
    }

    @Override
    public boolean enabled() {
      return true;
    }

    @Override
    public void init() {
      // no-op
    }

    @Override
    public void run() {
      // no-op
    }

    @Override
    public void destroy() {
      // no-op
    }
  }

  static final class FakeRegistrationInstance implements RegistrationInstance {
    private final Map<String, String> properties = new HashMap<>();

    @Override
    public Map<String, String> getProperties() {
      return properties;
    }

    @Override
    public String getEnvironment() {
      return "";
    }

    @Override
    public String getApplication() {
      return "app";
    }

    @Override
    public String getServiceName() {
      return "provider";
    }

    @Override
    public String getAlias() {
      return "";
    }

    @Override
    public String getVersion() {
      return "1.0.0";
    }

    @Override
    public DataCenterInfo getDataCenterInfo() {
      return null;
    }

    @Override
    public String getDescription() {
      return "";
    }

    @Override
    public Map<String, String> getSchemas() {
      return new HashMap<>();
    }

    @Override
    public List<String> getEndpoints() {
      return List.of();
    }

    @Override
    public String getInstanceId() {
      return "fake-instance";
    }

    @Override
    public MicroserviceInstanceStatus getStatus() {
      return MicroserviceInstanceStatus.UP;
    }
  }
}
