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
package org.apache.servicecomb.match.marker.operator;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RegexOperator implements MatchOperator {

  private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  /**
   * 该功能慎用，如果正则复杂+并发高效率低
   *
   * @param targetStr
   * @param patternStr
   * @return
   */
  @Override
  public boolean match(String targetStr, String patternStr) {
    Future<Boolean> f = executor.submit(() -> targetStr.matches(patternStr));
    try {
      return f.get(1, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(
          "operator regex failed or timeout,targetStr: " + targetStr + " ,patternStr:" + patternStr
              + " ,please check.");
    }
  }
}
