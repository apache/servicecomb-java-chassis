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
package org.apache.servicecomb.match.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.match.propertirs.MatchProperties;
import org.apache.servicecomb.match.marker.GovHttpRequest;
import org.apache.servicecomb.match.marker.Matcher;
import org.apache.servicecomb.match.marker.RequestProcessor;
import org.apache.servicecomb.match.marker.TrafficMarker;


public class MatchersServiceImpl implements MatchersService {

  private RequestProcessor requestProcessor = new RequestProcessor();

  private MatchProperties matchProperties = new MatchProperties();

  /**
   * @param govHttpRequest
   * @return
   */
  @Override
  public List<String> getMatchStr(GovHttpRequest govHttpRequest) {
    Map<String, TrafficMarker> map = matchProperties.covert();
    List<String> marks = new ArrayList<>();
    for (Entry<String, TrafficMarker> entry : map.entrySet()) {
      for (Matcher match : entry.getValue().getMatches()) {
        if (requestProcessor.match(govHttpRequest, match)) {
          marks.add(entry.getKey() + "." + match.getName());
        }
      }
    }
    return marks;
  }
}
