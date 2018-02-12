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

package org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder;

/**
 * record what kinds of access log item we support
 */
public enum AccessLogItemTypeEnum {
  TEXT_PLAIN,
  // %m, cs-method
  HTTP_METHOD,
  // %s, sc-status
  HTTP_STATUS,
  // %T
  DURATION_IN_SECOND,
  // %D
  DURATION_IN_MILLISECOND,
  // %h
  REMOTE_HOSTNAME,
  // %v
  LOCAL_HOSTNAME,
  // %p
  LOCAL_PORT,
  // %B
  RESPONSE_SIZE,
  // %b
  RESPONSE_SIZE_CLF,
  // %r
  FIRST_LINE_OF_REQUEST,
  // %U, cs-uri-stem
  URL_PATH,
  // %q, cs-uri-query
  QUERY_STRING,
  // cs-uri
  URL_PATH_WITH_QUERY,
  //%H
  REQUEST_PROTOCOL,
  // %t
  DATETIME_DEFAULT,
  // %{PATTERN}t, %{PATTERN|TIMEZONE|LOCALE}t
  DATETIME_CONFIGURABLE,
  // %{VARNAME}i
  REQUEST_HEADER,
  // %{VARNAME}o
  RESPONSE_HEADER,
  // %{VARNAME}C
  COOKIE,
  // %SCB-traceId
  SCB_TRACE_ID;
}
