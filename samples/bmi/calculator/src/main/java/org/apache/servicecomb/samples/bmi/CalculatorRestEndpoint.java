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

package org.apache.servicecomb.samples.bmi;

import java.util.Date;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * {@link CalculatorRestEndpoint} provides the rest implementation of {@link CalculatorEndpoint}.
 * The rest endpoint is accessed by /bmi?height={value}&width={value} with HTTP GET.
 */
@RestSchema(schemaId = "calculatorRestEndpoint")
@RequestMapping(path = "/")
public class CalculatorRestEndpoint implements CalculatorEndpoint {

  private final CalculatorService calculatorService;

  private final InstanceInfoService instanceInfoService;

  @Autowired
  public CalculatorRestEndpoint(CalculatorService calculatorService, InstanceInfoService instanceInfoService) {
    this.calculatorService = calculatorService;
    this.instanceInfoService = instanceInfoService;
  }

  @GetMapping(path = "/bmi")
  @Override
  public BMIViewObject calculate(double height, double weight) {

    String instanceId = instanceInfoService.getInstanceId();
    double bmiResult = calculatorService.calculate(height, weight);
    return new BMIViewObject(bmiResult, instanceId, new Date());
  }

}
