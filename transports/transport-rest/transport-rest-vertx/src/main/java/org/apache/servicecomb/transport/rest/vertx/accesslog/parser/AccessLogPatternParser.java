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

package org.apache.servicecomb.transport.rest.vertx.accesslog.parser;

import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;

/**
 * This parser will parse the rawPattern of access log and generate a list of {@link AccessLogItem},
 * which will be used in {@link org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogGenerator} to generate
 * access log content.
 * @param <T> the type of {@linkplain org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogParam#contextData
 * AccessLogParam.contextData}, which usually depends on the transport way.
 */
public interface AccessLogPatternParser<T> {
  List<AccessLogItem<T>> parsePattern(String rawPattern);
}
