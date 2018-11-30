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

package org.apache.servicecomb.foundation.common.encrypt;

import org.junit.Assert;
import org.junit.Test;

public class TestEncryptions {
  class MyEncryption implements Encryption {

    @Override
    public char[] decode(char[] encrypted, String tags) {
      if (tags == null) {
        return null;
      }
      return encrypted;
    }

    @Override
    public char[] encode(char[] plain, String tags) {
      if (tags == null) {
        return null;
      }
      return plain;
    }
  }

  @Test
  public void testEncryptions() {
    Assert.assertEquals(Encryptions.decode((String) null, ""), null);
    Assert.assertEquals(Encryptions.decode("abcd", ""), "abcd");
    Assert.assertEquals(Encryptions.decode("abcd", null), "abcd");
    Assert.assertEquals(Encryptions.encode((String) null, ""), null);
    Assert.assertEquals(Encryptions.encode("abcd", ""), "abcd");
    Assert.assertEquals(Encryptions.decode("abcd", null), "abcd");
  }

  @Test
  public void testEncryptionsMy() {
    Encryption old = Encryptions.getEncryption();
    Encryptions.setEncryption(new MyEncryption());
    Assert.assertEquals(Encryptions.decode((String) null, ""), null);
    Assert.assertEquals(Encryptions.decode("abcd", ""), "abcd");
    Assert.assertEquals(Encryptions.decode("abcd", null), null);
    Assert.assertEquals(Encryptions.encode((String) null, ""), null);
    Assert.assertEquals(Encryptions.encode("abcd", ""), "abcd");
    Assert.assertEquals(Encryptions.encode("abcd", null), null);
    Encryptions.setEncryption(old);
  }
}
