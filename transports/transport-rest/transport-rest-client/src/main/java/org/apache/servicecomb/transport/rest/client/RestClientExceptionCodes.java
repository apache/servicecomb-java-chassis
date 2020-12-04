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
package org.apache.servicecomb.transport.rest.client;

public interface RestClientExceptionCodes {
  String FAILED_TO_CREATE_REST_CLIENT_TRANSPORT_CONTEXT = "scb_rest_client.40000000";

  String FAILED_TO_ENCODE_REST_CLIENT_REQUEST = "scb_rest_client.40000001";

  String FAILED_TO_DECODE_REST_SUCCESS_RESPONSE = "scb_rest_client.40000002";

  String FAILED_TO_DECODE_REST_FAIL_RESPONSE = "scb_rest_client.40000003";
}
