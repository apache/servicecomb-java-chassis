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

import java.net.InetAddress;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestLocalAddressCache {
  @AfterEach
  public void tearDown() {
    LocalAddressCache.reset();
  }

  @Test
  public void loopbackAddressesAreNotLocalInstanceAddresses() {
    Assertions.assertFalse(LocalAddressCache.isLocalAddress("127.0.0.1"));
    Assertions.assertFalse(LocalAddressCache.isLocalAddress("localhost"));
    Assertions.assertFalse(LocalAddressCache.isLocalAddress("::1"));
  }

  @Test
  public void loopbackAddressesAreDetected() {
    Assertions.assertTrue(LocalAddressCache.isLoopbackAddress("127.0.0.1"));
    Assertions.assertTrue(LocalAddressCache.isLoopbackAddress("localhost"));
    Assertions.assertTrue(LocalAddressCache.isLoopbackAddress("::1"));
    Assertions.assertFalse(LocalAddressCache.isLoopbackAddress("192.168.1.100"));
  }

  @Test
  public void nullOrEmptyAreNotLocal() {
    Assertions.assertFalse(LocalAddressCache.isLocalAddress(null));
    Assertions.assertFalse(LocalAddressCache.isLocalAddress(""));
    Assertions.assertFalse(LocalAddressCache.isLoopbackAddress(null));
    Assertions.assertFalse(LocalAddressCache.isLoopbackAddress(""));
  }

  @Test
  public void localInterfaceAddressIsLocal() throws Exception {
    InetAddress localHost = InetAddress.getLocalHost();
    if (localHost.isLoopbackAddress()) {
      return;
    }
    Assertions.assertTrue(LocalAddressCache.isLocalAddress(localHost.getHostAddress()));
    Assertions.assertTrue(LocalAddressCache.getLocalAddresses().contains(localHost.getHostName()));
  }

  @Test
  public void remoteAddressIsNotLocal() {
    Assertions.assertFalse(LocalAddressCache.isLocalAddress("10.255.254.253"));
  }

  @Test
  public void resetClearsCache() {
    LocalAddressCache.getLocalAddresses();
    LocalAddressCache.reset();
    Assertions.assertNotNull(LocalAddressCache.getLocalAddresses());
  }
}
