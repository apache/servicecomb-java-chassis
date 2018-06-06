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

/**
 * Hold a group of {@link VertxRestAccessLogItemMeta} so that user can define
 * only one VertxRestAccessLogItemMeta in spi loading file and load a group of meta.
 * 
 * Once the access log loading mechanism finds that a meta is CompositeVertxRestAccessLogItemMeta,
 * the meta hold by it will be used in access log while this meta itself will be ignored.
 */
public abstract class CompositeVertxRestAccessLogItemMeta extends VertxRestAccessLogItemMeta {
  public abstract List<VertxRestAccessLogItemMeta> getAccessLogItemMetas();
}
