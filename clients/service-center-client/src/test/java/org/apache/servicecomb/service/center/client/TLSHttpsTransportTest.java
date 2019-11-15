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

package org.apache.servicecomb.service.center.client;

import org.apache.servicecomb.service.center.client.http.HttpTransport;
import org.apache.servicecomb.service.center.client.http.TLSConfig;
import org.apache.servicecomb.service.center.client.http.TLSHttpsTransport;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by   on 2019/10/31.
 */
public class TLSHttpsTransportTest {

  /**
   * test TLS HttpTransport
   */
  @Test
  public void TestTLSConfig() {

    TLSConfig tlsConfig = new TLSConfig();

    tlsConfig.setKeyStoreType(TLSConfig.KeyStoreInstanceType.PKCS12);
    tlsConfig.setKeyStore(this.getClass().getResource("/tls/client.p12").getFile().toString());
    tlsConfig.setKeyStoreValue("123456");

    tlsConfig.setTrustStore(this.getClass().getResource("/tls/server.jks").getFile().toString());
    tlsConfig.setTrustStoreValue("123456");

    HttpTransport tlsHttpTransport = new TLSHttpsTransport(tlsConfig);

    Assert.assertNotNull(tlsHttpTransport);
  }
}
