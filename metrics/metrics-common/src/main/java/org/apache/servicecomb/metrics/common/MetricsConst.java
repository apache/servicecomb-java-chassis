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

public class MetricsConst {
  public static final String CONSUMER_PREFIX_TEMPLATE = "servicecomb.%s.consumer";

  public static final String PRODUCER_PREFIX_TEMPLATE = "servicecomb.%s.producer";

  public static final String INSTANCE_CONSUMER_PREFIX = String.format(CONSUMER_PREFIX_TEMPLATE, "instance");

  public static final String INSTANCE_PRODUCER_PREFIX = String.format(PRODUCER_PREFIX_TEMPLATE, "instance");
}
