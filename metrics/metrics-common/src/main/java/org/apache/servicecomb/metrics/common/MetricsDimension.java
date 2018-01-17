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

package org.apache.servicecomb.metrics.common;

public class MetricsDimension {
  public static final String DIMENSION_STATUS = "Status";

  public static final String DIMENSION_STATUS_ALL = "all";

  public static final String DIMENSION_STATUS_SUCCESS_FAILED_SUCCESS = "success";

  public static final String DIMENSION_STATUS_SUCCESS_FAILED_FAILED = "failed";

  public static final String DIMENSION_STATUS_CODE_GROUP_1XX = "1xx";

  public static final String DIMENSION_STATUS_CODE_GROUP_2XX = "2xx";

  public static final String DIMENSION_STATUS_CODE_GROUP_3XX = "3xx";

  public static final String DIMENSION_STATUS_CODE_GROUP_4XX = "4xx";

  public static final String DIMENSION_STATUS_CODE_GROUP_5XX = "5xx";

  public static final String DIMENSION_STATUS_CODE_GROUP_OTHER = "xxx";

  public static final String DIMENSION_STATUS_OUTPUT_LEVEL_SUCCESS_FAILED = "success_failed";

  public static final String DIMENSION_STATUS_OUTPUT_LEVEL_CODE_GROUP = "code_group";

  public static final String DIMENSION_STATUS_OUTPUT_LEVEL_CODE = "code";
}
