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
package com.huaweicloud.governance.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.huaweicloud.governance.marker.GovHttpRequest;
import com.huaweicloud.governance.properties.MatchProperties;
import com.huaweicloud.governance.marker.Matcher;
import com.huaweicloud.governance.marker.RequestProcessor;
import com.huaweicloud.governance.marker.TrafficMarker;

import java.util.List;

public class MatchersServiceImpl implements MatchersService {

  @Autowired
  private RequestProcessor requestProcessor;

  @Autowired
  private MatchProperties matchProperties;

  @Value("spring.cloud.servicecomb.discovery.version")
  private String version;

  @Value("${spring.cloud.servicecomb.discovery.serviceName:${spring.application.name:}}")
  private String serviceName;

  /**
   *
   * @param govHttpRequest
   * @return
   */
  @Override
  public List<String> getMatchStr(GovHttpRequest govHttpRequest) {
    Map<String, TrafficMarker> map = matchProperties.covert();
    List<String> marks = new ArrayList<>();
    for (Entry<String, TrafficMarker> entry : map.entrySet()) {
      //过滤服务名和版本
      if (!StringUtils.isEmpty(entry.getValue().getServices())) {
        String[] services = entry.getValue().getServices().split(",");
        boolean matchService = Arrays.stream(services).anyMatch(ser -> {
          String[] serAndVer = ser.split(":");
          if (serAndVer.length == 1) {
            return serviceName.equals(serAndVer[0]);
          } else if (serAndVer.length == 2) {
            return serviceName.equals(serAndVer[0]) && version.equals(serAndVer[1]);
          } else {
            return false;
          }
        });
        if (!matchService) {
          continue;
        }
      }
      for (Matcher match : entry.getValue().getMatches()) {
        if (requestProcessor.match(govHttpRequest, match)) {
          marks.add(entry.getKey() + "." + match.getName());
        }
      }
    }
    return marks;
  }
}
