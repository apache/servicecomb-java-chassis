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

package org.apache.servicecomb.darklaunch.oper;

import java.util.regex.Pattern;

public class LikeCondition extends AbstractCondition {
  private final Pattern pattern;

  public LikeCondition(String key, String expected) {
    super(key, expected);

    char[] cs = expected.toCharArray();
    StringBuilder regExp = new StringBuilder();
    int lastPos = 0;
    for (int i = 0; i < cs.length; i++) {
      if ((cs[i]) == '*') {
        regExp.append(Pattern.quote(new String(cs, lastPos, i - lastPos)));
        regExp.append(".*");
        lastPos = i + 1;
      } else if (cs[i] == '?') {
        regExp.append(Pattern.quote(new String(cs, lastPos, i - lastPos)));
        regExp.append(".");
        lastPos = i = 1;
      }
    }
    regExp.append(Pattern.quote(new String(cs, lastPos, cs.length - lastPos)));
    pattern = Pattern.compile(regExp.toString());
  }

  @Override
  public boolean match() {
    SupportedType type = this.getType();
    if (type == SupportedType.STRING) {
      return  this.pattern.matcher((String) this.getActual()).matches();
    } else {
      return false;
    }
  }
}
