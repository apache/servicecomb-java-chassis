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
package org.apache.servicecomb.match;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.match.marker.GovHttpRequest;
import org.apache.servicecomb.match.marker.Matcher;
import org.apache.servicecomb.match.marker.TrafficMarker;
import org.apache.servicecomb.match.marker.operator.RawOperator;
import org.apache.servicecomb.match.propertirs.MatchProperties;
import org.apache.servicecomb.match.service.MatchersService;
import org.apache.servicecomb.match.service.MatchersServiceImpl;
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class TestMatchersService {

  private MatchersService matchersService = new MatchersServiceImpl();

  private Map<String, TrafficMarker> getMockMatch() {
    Map<String, TrafficMarker> map = new HashMap<>();
    TrafficMarker trafficMarker = new TrafficMarker();
    Matcher matcher = new Matcher();
    RawOperator pathOperator = new RawOperator();
    pathOperator.put("regex", "/.*");
    matcher.setApiPath(pathOperator);
    matcher.setName("xxx");
    trafficMarker.setMatches(Collections.singletonList(matcher));
    map.put("demo-group", trafficMarker);
    return map;
  }

  @Test
  public void testMatchStr(@Mocked MatchProperties matchProperties) {
    GovHttpRequest govHttpRequest = new GovHttpRequest();
    govHttpRequest.setMethod("GET");
    govHttpRequest.setUri("/test");
    govHttpRequest.setHeaders(new HashMap<>());
    new Expectations() {
      {
        matchProperties.covert();
        result = getMockMatch();
      }
    };
    List<String> list = matchersService.getMatchStr(govHttpRequest);
    Assert.assertEquals(1, list.size());
    Assert.assertEquals("demo-group.xxx", list.get(0));
  }
}
