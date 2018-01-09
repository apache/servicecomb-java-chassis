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
package io.servicecomb.demo.perf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
public class PerfConfiguration {
  public static String selfMicroserviceName;

  public static String nextMicroserviceName;

  public static int syncCount;

  public static int asyncCount;

  public static boolean sync;

  public static String producer;

  public static String id;

  public static int step;

  public static int all;

  public static boolean fromDB;

  public static int responseSize;

  public static String responseData;

  public static int redisClientCount;

  public static String redisHost;

  public static int redisPort;

  public static String redisPassword;

  @Value(value = "${service_description.name}")
  public void setSelfMicroserviceName(String selfMicroserviceName) {
    PerfConfiguration.selfMicroserviceName = selfMicroserviceName;

    // self: perf-1/perf-a
    // next: perf-2/perf-b
    char last = selfMicroserviceName.charAt(selfMicroserviceName.length() - 1);
    nextMicroserviceName =
        selfMicroserviceName.substring(0, selfMicroserviceName.length() - 1) + (char) (last + 1);
  }

  @Value(value = "${response-size}")
  public void setResponseSize(int responseSize) {
    PerfConfiguration.responseSize = responseSize;
    PerfConfiguration.responseData = Strings.repeat("a", responseSize);
  }

  @Value(value = "${sync-count}")
  public void setSyncCount(int syncCount) {
    PerfConfiguration.syncCount = syncCount;
  }

  @Value(value = "${async-count}")
  public void setAsyncCount(int asyncCount) {
    PerfConfiguration.asyncCount = asyncCount;
  }

  @Value(value = "${sync}")
  public void setSync(boolean sync) {
    PerfConfiguration.sync = sync;
  }

  @Value(value = "${producer}")
  public void setProducer(String producer) {
    PerfConfiguration.producer = producer;
  }

  @Value(value = "${id}")
  public void setId(String id) {
    PerfConfiguration.id = id;
  }

  @Value(value = "${step}")
  public void setStep(int step) {
    PerfConfiguration.step = step;
  }

  @Value(value = "${all}")
  public void setAll(int all) {
    PerfConfiguration.all = all;
  }

  @Value(value = "${fromDB}")
  public void setFromDB(boolean fromDB) {
    PerfConfiguration.fromDB = fromDB;
  }

  @Value(value = "${redis.client.count}")
  public void setRedisClientCount(int redisClientCount) {
    PerfConfiguration.redisClientCount = redisClientCount;
  }

  @Value(value = "${redis.host}")
  public void setRedisHost(String redisHost) {
    PerfConfiguration.redisHost = redisHost;
  }

  @Value(value = "${redis.port}")
  public void setRedisPort(int redisPort) {
    PerfConfiguration.redisPort = redisPort;
  }

  @Value(value = "${redis.password:null}")
  public void setRedisPassword(String redisPassword) {
    PerfConfiguration.redisPassword = redisPassword;
  }
}
