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
  public static final String DOMAIN_NAME = ConfigCenterConfig.INSTANCE.getDomainName();

  public static final String CURRENT_VERSION = ConfigCenterConfig.INSTANCE.getApiVersion();

  public static final String VERSION_V2 = "v2";

  public static final String PREFIX_V3 = String.format("/v3/%s/configuration", DOMAIN_NAME);

  public static final String MEMBERS;

  static {
    if (VERSION_V2.equals(CURRENT_VERSION)) {
      MEMBERS = "/members";
    } else {
      MEMBERS = PREFIX_V3 + "/members";
    }
  }

  public static final String REFRESH_ITEMS;

  static {
    if (VERSION_V2.equals(CURRENT_VERSION)) {
      REFRESH_ITEMS = "/configuration/v2/refresh/items";
    } else {
      REFRESH_ITEMS = PREFIX_V3 + "/refresh/items";
    }
  }

  public static final String ITEMS;

  static {
    if (VERSION_V2.equals(CURRENT_VERSION)) {
      ITEMS = "/configuration/v2/items";
    } else {
      ITEMS = PREFIX_V3 + "/items";
    }
  }
}
