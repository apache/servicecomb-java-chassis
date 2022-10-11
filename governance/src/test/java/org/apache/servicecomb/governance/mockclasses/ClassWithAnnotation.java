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

package org.apache.servicecomb.governance.mockclasses;

import org.apache.servicecomb.governance.mockclasses.service.MockProfileClassUserService;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.utils.CustomMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ClassWithAnnotation  implements CustomMatch {
    @Autowired
    MockProfileClassUserService service;

    @Override
    public boolean matchRequest(GovernanceRequest request, String parameters) {
        String profileValue = service.getUser();
        return Stream.of(parameters.split(",")).collect(Collectors.toSet()).contains(profileValue);
    }
}
