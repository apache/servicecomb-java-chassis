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
package org.apache.servicecomb.metrics.core.meter.invocation;

import com.netflix.spectator.api.Statistic;

public interface MeterInvocationConst {
  String INVOCATION_NAME = "servicecomb.invocation";

  // consumer or producer
  String TAG_ROLE = "role";

  String TAG_OPERATION = "operation";

  String TAG_TRANSPORT = "transport";

  String TAG_STAGE = "stage";

  String TAG_STATUS = "status";

  String TAG_STATISTIC = Statistic.count.key();

  String STAGE_TOTAL = "total";

  String STAGE_PREPARE = "prepare";

  String STAGE_EXECUTOR_QUEUE = "queue";

  String STAGE_EXECUTION = "execution";

  String EDGE_INVOCATION_NAME = "EDGE";

  String STAGE_HANDLERS_REQUEST = "handlers_request";

  String STAGE_HANDLERS_RESPONSE = "handlers_response";

  // producer only
  String STAGE_SERVER_FILTERS_REQUEST = "server_filters_request";

  String STAGE_SERVER_FILTERS_RESPONSE = "server_filters_response";

  String STAGE_PRODUCER_SEND_RESPONSE = "producer_send_response";

  //consumer only

  String STAGE_CLIENT_FILTERS_REQUEST = "client_filters_request";

  String STAGE_CONSUMER_SEND_REQUEST = "consumer_send_request";

  String STAGE_CONSUMER_GET_CONNECTION = "consumer_get_connection";

  String STAGE_CONSUMER_WRITE_TO_BUF = "consumer_write_to_buf";

  String STAGE_CONSUMER_WAIT_RESPONSE = "consumer_wait_response";

  String STAGE_CONSUMER_WAKE_CONSUMER = "consumer_wake_consumer";

  String STAGE_CLIENT_FILTERS_RESPONSE = "client_filters_response";
}
