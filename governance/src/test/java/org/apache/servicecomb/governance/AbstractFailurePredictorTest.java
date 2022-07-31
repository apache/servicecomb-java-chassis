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

package org.apache.servicecomb.governance;

import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.governance.handler.ext.AbstractFailurePredictor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AbstractFailurePredictorTest {
  class MyAbstractFailurePredictor extends AbstractFailurePredictor {
    MyAbstractFailurePredictor() {
    }

    @Override
    protected String extractStatusCode(Object result) {
      return (String) result;
    }

    @Override
    public boolean isFailedResult(Throwable e) {
      return super.isFailedResult(e);
    }
  }

  @Test
  public void testCodeMatch() {
    AbstractFailurePredictor predictor = new MyAbstractFailurePredictor();
    List<String> statusList = Arrays.asList("500");
    Assertions.assertTrue(predictor.isFailedResult(statusList, "500"));
    Assertions.assertFalse(predictor.isFailedResult(statusList, "502"));
    Assertions.assertFalse(predictor.isFailedResult(statusList, "400"));
    Assertions.assertFalse(predictor.isFailedResult(statusList, "444"));

    statusList = Arrays.asList("5x0");
    Assertions.assertTrue(predictor.isFailedResult(statusList, "500"));
    Assertions.assertFalse(predictor.isFailedResult(statusList, "502"));
    Assertions.assertFalse(predictor.isFailedResult(statusList, "400"));
    Assertions.assertFalse(predictor.isFailedResult(statusList, "444"));

    statusList = Arrays.asList(null, "xx", "5x0");
    Assertions.assertTrue(predictor.isFailedResult(statusList, "500"));
    Assertions.assertFalse(predictor.isFailedResult(statusList, "502"));
    Assertions.assertFalse(predictor.isFailedResult(statusList, "400"));
    Assertions.assertFalse(predictor.isFailedResult(statusList, "444"));
  }
}
