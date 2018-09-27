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
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot;
import org.apache.servicecomb.foundation.protobuf.internal.model.Root;
import org.apache.servicecomb.foundation.protobuf.performance.engine.Jackson;
import org.apache.servicecomb.foundation.protobuf.performance.engine.Protobuf;
import org.apache.servicecomb.foundation.protobuf.performance.engine.Protostuff;
import org.apache.servicecomb.foundation.protobuf.performance.engine.ScbStrong;
import org.apache.servicecomb.foundation.protobuf.performance.engine.ScbWeak;

public abstract class TestBase {
  public interface Case {
    TestEngineResult run();
  }

  static ProtubufCodecEngine scbStrong = new ScbStrong();

  static ProtubufCodecEngine scbWeak = new ScbWeak();

  static ProtubufCodecEngine protoStuff = new Protostuff();

  static ProtubufCodecEngine protobuf = new Protobuf();

  static ProtubufCodecEngine jackson = new Jackson();

  protected Root pojoRoot = new Root();

  protected Map<String, Object> pojoRootMap;

  protected ProtobufRoot.Root.Builder builder = ProtobufRoot.Root.newBuilder();

  @SuppressWarnings("unchecked")
  public TestResult run(int count) throws IOException {
    pojoRootMap = (Map<String, Object>) Jackson.jsonMapper.convertValue(pojoRoot, Map.class);
    // warm
    doRun(10_000);

    // real test
    return doRun(count);
  }

  private TestResult doRun(int count) throws IOException {
    TestResult testResult = new TestResult();
    testResult.name = this.getClass().getSimpleName();

    testResult.addTestEngineResult(runOneEngine(protoStuff, pojoRoot, count));
    testResult.addTestEngineResult(runOneEngine(scbStrong, pojoRoot, count));
    testResult.addTestEngineResult(runOneEngine(scbWeak, pojoRootMap, count));
    testResult.addTestEngineResult(runOneEngine(protobuf, builder, count));
    testResult.addTestEngineResult(runOneEngine(jackson, pojoRoot, count));
    return testResult;
  }

  private TestEngineResult runOneEngine(ProtubufCodecEngine engine, Object model, int count)
      throws IOException {
    TestEngineResult engineResult = new TestEngineResult();
    engineResult.engineName = engine.getClass().getSimpleName();

    // serialize
    {
      long msStart = System.currentTimeMillis();
      for (int idx = 0; idx < count; idx++) {
        engineResult.serBytes = engine.serialize(model);
      }
      long msEnd = System.currentTimeMillis();

      engineResult.msSerTime = msEnd - msStart;
    }

    // deserialize
    {
      long msStart = System.currentTimeMillis();
      for (int idx = 0; idx < count; idx++) {
        engineResult.deserResult = engine.deserialize(engineResult.serBytes);
      }
      long msEnd = System.currentTimeMillis();

      engineResult.msDeserTime = msEnd - msStart;
    }

    engineResult.deserResultBytes = engine.serialize(engineResult.deserResult);
    return engineResult;
  }
}
