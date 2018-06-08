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

package org.apache.servicecomb.config.client;

public class URIConst {
  private final String DOMAIN_NAME = ConfigCenterConfig.INSTANCE.getDomainName();

  private final String CURRENT_VERSION = ConfigCenterConfig.INSTANCE.getApiVersion();

  private final String VERSION_V2 = "v2";

  private final String PREFIX_V2 = "/configuration/v2";

  private final String PREFIX_V3 = String.format("/v3/%s/configuration", DOMAIN_NAME);

  private final boolean isV2 = VERSION_V2.equals(CURRENT_VERSION);

  private final String CURRENT_PREFIX = isV2 ? PREFIX_V2 : PREFIX_V3;

  // v2 no prefix
  public final String MEMBERS = isV2 ? "/members" : PREFIX_V3 + "/members";

  // v2 has prefix
  public final String REFRESH_ITEMS = CURRENT_PREFIX + "/refresh/items";

  public final String ITEMS = CURRENT_PREFIX + "/items";
}
