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

package org.apache.servicecomb.loadbalance;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.serviceregistry.cache.CacheEndpoint;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.Server;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestSessionSticknessRule {

  @Test
  public void testRuleFullOperation() {
    SessionStickinessRule rule = new SessionStickinessRule();

    LoadBalancer mockedLb = mock(LoadBalancer.class);
    Transport transport = mock(Transport.class);
    CseServer mockedServer = new CseServer(transport, new CacheEndpoint("rest:127.0.0.1:8889", null));
    Object key = Mockito.mock(Object.class);
    LoadBalancerStats stats = mock(LoadBalancerStats.class);
    Mockito.when(mockedLb.getLoadBalancerStats()).thenReturn(stats);
    Deencapsulation.invoke(rule, "chooseServerWhenTimeout", key);
    mockedServer.setAlive(true);
    mockedServer.setReadyToServe(true);
    List<Server> allServers = Arrays.asList(mockedServer);
    when(mockedLb.getReachableServers()).thenReturn(allServers);
    when(mockedLb.getAllServers()).thenReturn(allServers);

    rule.setLoadBalancer(mockedLb);
    Assert.assertEquals(rule.getLoadBalancer(), mockedLb);
    Server s = rule.choose("default");
    Assert.assertEquals(s, mockedServer);

    s = rule.choose("default");
    Assert.assertEquals(s, mockedServer);
  }

  @Test
  public void testServerWithoutTimeoutAndWithThreshold() {

    boolean status = true;

    SessionStickinessRule ss = new SessionStickinessRule();

    Object key = new Object();

    Server s = new Server("test");

    Deencapsulation.setField(ss, "lastServer", s);

    new MockUp<SessionStickinessRule>() {

      @Mock
      private boolean isTimeOut() {
        return false;
      }
    };

    new MockUp<SessionStickinessRule>() {

      @Mock
      private boolean isErrorThresholdMet() {
        return true;
      }
    };

    try {
      ss.choose(key);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);
  }

  @Test
  public void testServerWithTimeout() {

    boolean status = true;

    SessionStickinessRule ss = new SessionStickinessRule();

    Object key = new Object();

    Server s = new Server("test");

    Deencapsulation.setField(ss, "lastServer", s);

    new MockUp<SessionStickinessRule>() {

      @Mock
      private boolean isTimeOut() {
        return true;
      }
    };

    try {
      ss.choose(key);
    } catch (Exception e) {
      status = false;
    }

    Assert.assertTrue(status);
  }

  @Test
  public void testServerWithoutTimeoutException() {

    boolean status = true;

    SessionStickinessRule ss = new SessionStickinessRule();

    Object key = new Object();

    Server s = new Server("test");

    Deencapsulation.setField(ss, "lastServer", s);

    new MockUp<SessionStickinessRule>() {

      @Mock
      private boolean isTimeOut() {
        return false;
      }
    };

    try {
      ss.choose(key);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertFalse(status);
  }

  @Test
  public void testServerWithoutTimeoutAndThreshold() {

    boolean status = true;

    SessionStickinessRule ss = new SessionStickinessRule();

    Object key = new Object();

    Server s = new Server("test");

    Deencapsulation.setField(ss, "lastServer", s);

    new MockUp<SessionStickinessRule>() {

      @Mock
      private boolean isTimeOut() {
        return false;
      }
    };

    new MockUp<SessionStickinessRule>() {

      @Mock
      private boolean isErrorThresholdMet() {
        return false;
      }
    };

    try {
      ss.choose(key);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);
  }

  @Test
  public void testServerWithActualServerObj() {

    boolean status = true;
    SessionStickinessRule ss = new SessionStickinessRule();

    Object key = new Object();

    Server s = new Server("test");

    Deencapsulation.setField(ss, "lastServer", s);
    try {
      ss.choose(key);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);
  }

  @Test
  public void testServerWithKey() {

    boolean status = true;
    SessionStickinessRule ss = new SessionStickinessRule();

    Object key = new Object();
    try {
      ss.choose(key);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);
  }
}
