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
package org.apache.servicecomb.demo.perf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

// git log --author="wujimin" --pretty=tformat:%s --shortstat --since ==2017-9-1
public class Code {
  static File file = new File("d:/work/git/incubator-servicecomb-java-chassis/0218.txt");

  public static void main(String[] args) throws IOException {
    List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);

    int totalAdd = 0;
    int totalDel = 0;
    List<String> output = new ArrayList<>();
    System.out.println(lines.size());
    for (int idx = 0; idx < lines.size(); ) {
      String msg = lines.get(idx);

      // skip a empty line
      idx += 2;
      // 2 files changed, 2 insertions(+), 2 deletions(-)
      String line = lines.get(idx);
      idx++;
//      System.out.println(idx + ": " + msg);

      int add = 0;
      int delete = 0;
      for (String part : line.split(",")) {
        String key = " insertions(+)";
        int matchIdx = part.indexOf(key);
        if (matchIdx > 0) {
          add = Integer.valueOf(part.substring(0, matchIdx).trim());
          continue;
        }

        key = " deletions(-)";
        matchIdx = part.indexOf(key);
        if (matchIdx > 0) {
          delete = Integer.valueOf(part.substring(0, matchIdx).trim());
          continue;
        }
      }

      totalAdd += add;
      totalDel += delete;
      output.add(String.format("%d | %d | %s", add, delete, msg));
    }

    output.add(String.format("summary, add: %d, del: %s", totalAdd, totalDel));
    System.out.println(String.join("\n", output));
  }
}
