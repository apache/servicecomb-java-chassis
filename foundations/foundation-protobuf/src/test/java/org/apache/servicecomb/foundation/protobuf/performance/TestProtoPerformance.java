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
package org.apache.servicecomb.foundation.protobuf.performance;

import java.io.IOException;

import org.apache.servicecomb.foundation.protobuf.performance.cases.Empty;
import org.apache.servicecomb.foundation.protobuf.performance.cases.Map;
import org.apache.servicecomb.foundation.protobuf.performance.cases.Mixed;
import org.apache.servicecomb.foundation.protobuf.performance.cases.PojoList;
import org.apache.servicecomb.foundation.protobuf.performance.cases.Scalars;
import org.apache.servicecomb.foundation.protobuf.performance.cases.SimpleList;

import com.google.common.base.Strings;

public class TestProtoPerformance {
  public static void main(String[] args) throws IOException {
    System.out.println("1.protobuf\n"
        + "  in our real scenes\n"
        + "  business model never bind to transport, and can switch between different transports dynamically\n"
        + "  that means if we choose standard protobuf, must build protobuf models from business models each time\n"
        + "  so should be much slower than the test results");
    System.out.println("2.protoStuff\n"
        + "  some scenes, there is no field but have getter or setter, so we can not use unsafe to access field\n"
        + "  so we disable protoStuff unsafe feature");
    System.out.println("3.jackson\n"
        + "  not support map, so skip map in map/mixed test");
    System.out.println("4.serialize result size\n"
        + "  ScbStrong/ScbWeak/Protobuf have the same and smaller size, because skip all default/null value");
    System.setProperty("protostuff.runtime.use_sun_misc_unsafe", "false");

    int count = 50_0000;

    printResult(new Empty().run(count));
    printResult(new Scalars().run(count));
    printResult(new SimpleList().run(count));
    printResult(new PojoList().run(count));
    printResult(new Map().run(count));
    printResult(new Mixed().run(count));
  }

  private static void printResult(TestResult result) {
    String strFmt = Strings.repeat("%-11s", result.engineResults.size());
    String numberFmt = Strings.repeat("%-11d", result.engineResults.size());

    System.out.println(result.name + ":");
    System.out.printf("               " + strFmt + "\n",
        result.engineResults.stream().map(r -> r.engineName).toArray());

    System.out.printf("ser time(ms)  :" + numberFmt + "\n",
        result.engineResults.stream().map(r -> r.msSerTime).toArray());
    System.out.printf("ser len       :" + numberFmt + "\n",
        result.engineResults.stream().map(r -> r.serBytes.length).toArray());

    System.out.printf("deser time(ms):" + numberFmt + "\n",
        result.engineResults.stream().map(r -> r.msDeserTime).toArray());
    System.out.printf("deser-ser len :" + numberFmt + "\n\n",
        result.engineResults.stream().map(r -> r.deserResultBytes.length).toArray());
  }
}
