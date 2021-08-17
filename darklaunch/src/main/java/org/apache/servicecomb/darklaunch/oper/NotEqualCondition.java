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

public class NotEqualCondition extends AbstractCondition {

  public NotEqualCondition(String key, String expected) {
    super(key, expected);
  }

  @Override
  public boolean match() {
    SupportedType type = this.getType();
    if (type == SupportedType.NUMBER) {
      return compareNum(this.getActual(), this.expected()) != 0;
    } else if (type == SupportedType.STRING) {
      return ((String) this.getActual()).compareTo(this.expected()) != 0;
    } else {
      return false;
    }
  }
}
