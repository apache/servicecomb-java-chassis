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

package org.apache.servicecomb.registry.lightweight.model;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.StringUtils;

public class MicroserviceInstances {
  private boolean microserviceNotExist;

  private boolean needRefresh = true;

  private String revision;

  private FindInstancesResponse instancesResponse;

  public boolean isMicroserviceNotExist() {
    return microserviceNotExist;
  }

  public MicroserviceInstances setMicroserviceNotExist(boolean microserviceNotExist) {
    this.microserviceNotExist = microserviceNotExist;
    return this;
  }

  public boolean isNeedRefresh() {
    return needRefresh;
  }

  public MicroserviceInstances setNeedRefresh(boolean needRefresh) {
    this.needRefresh = needRefresh;
    return this;
  }

  public String getRevision() {
    return revision;
  }

  public MicroserviceInstances setRevision(String revision) {
    this.revision = revision;
    return this;
  }

  public FindInstancesResponse getInstancesResponse() {
    return instancesResponse;
  }

  public MicroserviceInstances setInstancesResponse(FindInstancesResponse instancesResponse) {
    this.instancesResponse = instancesResponse;
    return this;
  }

  public void mergeMicroserviceInstances(MicroserviceInstances other) {
    mergeNeedRefresh(other.needRefresh);
    mergeMicroserviceNotExist(other.microserviceNotExist);
    mergeRevision(other);
    mergeInstanceResponse(other.getInstancesResponse());
  }

  private void mergeRevision(MicroserviceInstances other) {
    if (!other.isMicroserviceNotExist() && other.needRefresh) {
      Mac mac = HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_1, stringToBytes(this.revision));
      this.revision = Base64.encodeBase64String(mac.doFinal(stringToBytes(other.revision)));
    }
  }

  private byte[] stringToBytes(String input) {
    if (StringUtils.isEmpty(input)) {
      input = "@";
    }
    return input.getBytes(StandardCharsets.UTF_8);
  }

  private void mergeMicroserviceNotExist(boolean microserviceNotExist) {
    // only is all not exists, set to not exits.
    if (this.microserviceNotExist) {
      this.microserviceNotExist = microserviceNotExist;
    }
  }

  private void mergeNeedRefresh(boolean needRefresh) {
    // if one of discovery need refresh, all need refresh
    if (!this.needRefresh) {
      this.needRefresh = needRefresh;
    }
  }

  private void mergeInstanceResponse(FindInstancesResponse instancesResponse) {
    if (instancesResponse == null) {
      return;
    }

    if (this.instancesResponse == null) {
      this.instancesResponse = instancesResponse;
      return;
    }

    this.instancesResponse.mergeInstances(instancesResponse.getInstances());
  }
}
