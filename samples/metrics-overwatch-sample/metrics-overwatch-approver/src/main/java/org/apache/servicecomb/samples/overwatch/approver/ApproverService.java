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

package org.apache.servicecomb.samples.overwatch.approver;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@RestSchema(schemaId = "approverServiceEndpoint")
@RequestMapping(path = "/")
public class ApproverService {

  private static RestTemplate restTemplate = RestTemplateBuilder.create();

  @GetMapping(path = "/audit")
  public Boolean audit(double amount) {
    if (amount <= 10000) {
      boolean resultSupervisor = restTemplate.getForObject("cse://Supervisor/audit?amount=" +
          String.format("%.2f", amount), Boolean.class);

      boolean resultFinance = restTemplate.getForObject("cse://Finance/audit?amount=" +
          String.format("%.2f", amount), Boolean.class);
      return resultFinance && resultSupervisor;
    }
    return false;
  }
}
