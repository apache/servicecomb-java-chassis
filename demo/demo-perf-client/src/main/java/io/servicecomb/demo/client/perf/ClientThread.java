/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.demo.client.perf;

import io.servicecomb.demo.pojo.client.PojoClient;
import io.servicecomb.demo.server.Test;
import io.servicecomb.demo.server.TestRequest;
import io.servicecomb.demo.server.User;
import io.servicecomb.foundation.common.CommonThread;

public class ClientThread extends CommonThread {
  @Override
  public void run() {
    Test test = PojoClient.test;

    while (isRunning()) {
      int idx = 0;
      for (;;) {
        User user = new User();

        TestRequest request = new TestRequest();
        request.setUser(user);
        request.setIndex(idx);
        request.setData(PojoClient.buffer);

        try {
          User result = test.wrapParam(request);

          if (result.getIndex() != idx) {
            System.out.printf("error result:%s, expect idx %d\n", result, idx);
          }
        } catch (Throwable e) {
          //                    e.printStackTrace();
        }
      }
    }
  }
}
