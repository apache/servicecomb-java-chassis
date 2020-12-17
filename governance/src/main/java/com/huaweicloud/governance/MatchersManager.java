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
package com.huaweicloud.governance;

import com.huaweicloud.governance.marker.GovHttpRequest;
import com.huaweicloud.governance.policy.Policy;
import com.huaweicloud.governance.service.MatchersService;
import com.huaweicloud.governance.service.PolicyService;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
public class MatchersManager {

  @Autowired
  private MatchersService matchersService;

  @Autowired
  private PolicyService policyService;

  public MatchersManager() {
  }

  public Map<String, Policy> match(GovHttpRequest request) {
    /**
     * 1.获取该请求携带的marker
     */
    List<String> marks = matchersService.getMatchStr(request);
    /**
     * 2.通过 marker获取到所有的policy
     */
    return policyService.getAllPolicies(marks);
  }
}
