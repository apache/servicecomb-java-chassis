/*
 *  Copyright 2017 Huawei Technologies Co., Ltd
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.servicecomb.samples.bmi;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.servicecomb.provider.rest.common.RestSchema;

/**
 * {@link CalculatorRestEndpoint} provides the rest implementation of {@link CalculatorEndpoint}.
 * The rest endpoint is accessed by /bmi?height={value}&width={value} with HTTP GET.
 */
@RestSchema(schemaId = "calculatorRestEndpoint")
@RequestMapping("/")
@Controller
public class CalculatorRestEndpoint implements CalculatorEndpoint {

  private final CalculatorService calculatorService;

  @Autowired
  public CalculatorRestEndpoint(CalculatorService calculatorService) {
    this.calculatorService = calculatorService;
  }

  @Override
  @RequestMapping(value = "/bmi", method = RequestMethod.GET)
  @ResponseBody
  public double calculate(double height, double weight) {
    return calculatorService.calculate(height, weight);
  }
}
