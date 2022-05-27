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

package org.apache.servicecomb.foundation.bootstrap;

import org.apache.servicecomb.foundation.common.utils.SPIOrder;
import org.springframework.core.env.Environment;

/**
 * A boot strap service is loaded after Spring Environment is created.
 *
 * In boot strap service, user's can only read configurations from Environment. Dynamic configurations
 *
 * from config center is not available.
 *
 * e.g. an authentication service must be connected before connecting to config center.
 * e.g. an config center configurations must be read before connecting to config center.
 */
public interface BootStrapService extends SPIOrder {
  void startup(Environment environment);
}
