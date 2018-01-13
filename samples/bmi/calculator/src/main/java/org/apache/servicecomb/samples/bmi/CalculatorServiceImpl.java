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

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

@Service
public class CalculatorServiceImpl implements CalculatorService {

  /**
   * {@inheritDoc}
   */
  @Override
  public double calculate(double height, double weight) {
    if (height <= 0 || weight <= 0) {
      throw new IllegalArgumentException("Arguments must be above 0");
    }
    double heightInMeter = height / 100;
    double bmi = weight / (heightInMeter * heightInMeter);
    return roundToOnePrecision(bmi);
  }

  private double roundToOnePrecision(double value) {
    return new BigDecimal(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
  }
}
