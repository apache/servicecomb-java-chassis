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

package org.apache.servicecomb.foundation.ssl;

import javax.net.ssl.SSLSocketFactory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestSSLSocketFactoryExt {

  private SSLSocketFactoryExt instance = null;

  @Before
  public void setUp() throws Exception {
    SSLSocketFactory factory = Mockito.mock(SSLSocketFactoryExt.class);
    String[] ciphers = {"test"};

    String[] protos = {"test"};
    instance = new SSLSocketFactoryExt(factory, ciphers, protos);
  }

  @After
  public void tearDown() throws Exception {
    instance = null;
  }

  @Test
  public void testCreateSocketException() {
    boolean validAssert = true;
    try {
      instance.createSocket("host", 8080);
    } catch (Exception e) {
      validAssert = false;
    }
    Assert.assertFalse(validAssert);
  }
}
