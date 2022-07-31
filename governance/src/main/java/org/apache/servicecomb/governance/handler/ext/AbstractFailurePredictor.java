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
package org.apache.servicecomb.governance.handler.ext;

import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractFailurePredictor implements FailurePredictor {
  @Override
  public boolean isFailedResult(List<String> statusList, Object result) {
    String statusCode = extractStatusCode(result);
    if (StringUtils.isEmpty(statusCode)) {
      return false;
    }
    return statusCodeContains(statusList, statusCode);
  }

  protected abstract String extractStatusCode(Object result);

  protected static boolean statusCodeContains(List<String> statusList, String responseStatus) {
    return statusList.stream().anyMatch(status -> statusCodeMatch(status, responseStatus));
  }

  private static boolean statusCodeMatch(String status, String responseStatus) {
    if (status == null) {
      return false;
    }
    if (responseStatus.length() != status.length()) {
      return false;
    }
    char[] statusChar = status.toCharArray();
    char[] responseChar = responseStatus.toCharArray();
    return IntStream.range(0, statusChar.length).noneMatch(i ->
        statusChar[i] != responseChar[i] && statusChar[i] != 'x');
  }
}
