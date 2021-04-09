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

package org.apache.servicecomb.service.center.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.servicecomb.http.client.common.HttpUtils;

public class AddressManager {
  private final String projectName;

  private final List<String> addresses;

  private int index;

  public AddressManager(String projectName, List<String> addresses) {
    this.projectName = projectName;
    this.addresses = new ArrayList<>(addresses.size());
    addresses.forEach((address -> this.addresses.add(address)));
    this.index = new Random().nextInt(addresses.size());
  }

  private String formatAddress(String address) {
    try {
      return address + "/v4/" + HttpUtils.encodeURLParam(this.projectName);
    } catch (Exception e) {
      throw new IllegalStateException("not possible");
    }
  }

  public void changeAddress() {
    synchronized (this) {
      this.index++;
      if (this.index >= addresses.size()) {
        this.index = 0;
      }
    }
  }

  public boolean sslEnabled() {
    return address().startsWith("https://");
  }

  public String address() {
    synchronized (this) {
      return addresses.get(index);
    }
  }

  public String formatUrl(String url, boolean absoluteUrl) {
    return absoluteUrl ? address() + url : formatAddress(address()) + url;
  }
}
