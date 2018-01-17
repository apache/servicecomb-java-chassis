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

package org.apache.servicecomb.metrics.core.event.dimension;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status.Family;

import org.apache.servicecomb.metrics.common.MetricsDimension;

public class CodeGroupStatusConvertor implements StatusConvertor {

  private final Map<Family, String> families;

  public CodeGroupStatusConvertor() {
    this.families = new HashMap<>();
    this.families.put(Family.INFORMATIONAL, MetricsDimension.DIMENSION_STATUS_CODE_GROUP_1XX);
    this.families.put(Family.SUCCESSFUL, MetricsDimension.DIMENSION_STATUS_CODE_GROUP_2XX);
    this.families.put(Family.REDIRECTION, MetricsDimension.DIMENSION_STATUS_CODE_GROUP_3XX);
    this.families.put(Family.CLIENT_ERROR, MetricsDimension.DIMENSION_STATUS_CODE_GROUP_4XX);
    this.families.put(Family.SERVER_ERROR, MetricsDimension.DIMENSION_STATUS_CODE_GROUP_5XX);
    this.families.put(Family.OTHER, MetricsDimension.DIMENSION_STATUS_CODE_GROUP_OTHER);
  }

  @Override
  public String convert(boolean success, int statusCode) {
    return families.get(Family.familyOf(statusCode));
  }
}
