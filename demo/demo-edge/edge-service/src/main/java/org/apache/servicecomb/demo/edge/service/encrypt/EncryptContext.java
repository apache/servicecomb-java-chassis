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
package org.apache.servicecomb.demo.edge.service.encrypt;

import org.apache.servicecomb.demo.edge.authentication.encrypt.Hcr;

public class EncryptContext {
  private Hcr hcr;

  private String userId;

  public EncryptContext(Hcr hcr, String userId) {
    this.hcr = hcr;
    this.userId = userId;
  }

  public Hcr getHcr() {
    return hcr;
  }

  public void setHcr(Hcr hcr) {
    this.hcr = hcr;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
