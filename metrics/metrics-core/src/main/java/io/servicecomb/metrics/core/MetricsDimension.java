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

package io.servicecomb.metrics.core;

import io.servicecomb.foundation.common.exceptions.ServiceCombException;

public class MetricsDimension {
  public static final String DIMENSION_STATUS = "Status";

  public static final String DIMENSION_STATUS_ALL = "all";

  public static final String DIMENSION_STATUS_SUCCESS = "success";

  public static final String DIMENSION_STATUS_FAILED = "failed";

  public static String[] getDimensionOptions(String dimension) {
    if (DIMENSION_STATUS.equals(dimension)) {
      return new String[] {MetricsDimension.DIMENSION_STATUS_ALL,
          MetricsDimension.DIMENSION_STATUS_SUCCESS,
          MetricsDimension.DIMENSION_STATUS_FAILED};
    }
    throw new ServiceCombException("illegal dimension key : " + dimension);
  }
}
