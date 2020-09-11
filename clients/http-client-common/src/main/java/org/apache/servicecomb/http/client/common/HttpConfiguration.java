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

package org.apache.servicecomb.http.client.common;

import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;

public interface HttpConfiguration {
  class SSLProperties {
    private boolean enabled;

    private SSLOption sslOption;

    private SSLCustom sslCustom;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public SSLOption getSslOption() {
      return sslOption;
    }

    public void setSslOption(SSLOption sslOption) {
      this.sslOption = sslOption;
    }

    public SSLCustom getSslCustom() {
      return sslCustom;
    }

    public void setSslCustom(SSLCustom sslCustom) {
      this.sslCustom = sslCustom;
    }
  }
}
