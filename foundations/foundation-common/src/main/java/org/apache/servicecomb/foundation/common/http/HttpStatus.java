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
package org.apache.servicecomb.foundation.common.http;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

public class HttpStatus implements StatusType {
  public static boolean isSuccess(int code) {
    return Status.Family.SUCCESSFUL.equals(Status.Family.familyOf(code));
  }

  public static boolean isSuccess(StatusType status) {
    return Status.Family.SUCCESSFUL.equals(status.getFamily());
  }

  private final int statusCode;

  private final String reason;

  public HttpStatus(final int statusCode, final String reasonPhrase) {
    this.statusCode = statusCode;
    this.reason = reasonPhrase;
  }

  @Override
  public int getStatusCode() {
    return statusCode;
  }

  @Override
  public Family getFamily() {
    return Family.familyOf(statusCode);
  }

  @Override
  public String getReasonPhrase() {
    return reason;
  }
}
