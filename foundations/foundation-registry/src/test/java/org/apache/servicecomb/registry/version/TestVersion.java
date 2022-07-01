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

package org.apache.servicecomb.registry.version;

import org.apache.servicecomb.foundation.common.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestVersion {
  Version version;

  short s1 = 1;

  short s2 = 2;

  @Test
  public void constructFromStringNormalOnlyMajor() {
    version = new Version("1");
    Assertions.assertEquals("1.0.0.0", version.getVersion());
    Assertions.assertEquals(1, version.getMajor());
    Assertions.assertEquals(0, version.getMinor());
    Assertions.assertEquals(0, version.getPatch());
  }

  @Test
  public void constructFromStringNormalOnlyMajorMinor() {
    version = new Version("1.1");
    Assertions.assertEquals("1.1.0.0", version.getVersion());
    Assertions.assertEquals(1, version.getMajor());
    Assertions.assertEquals(1, version.getMinor());
    Assertions.assertEquals(0, version.getPatch());
  }

  @Test
  public void constructFromStringOnlyMajorMinorPatch() {
    version = new Version("1.1.1");
    Assertions.assertEquals("1.1.1.0", version.getVersion());
    Assertions.assertEquals(1, version.getMajor());
    Assertions.assertEquals(1, version.getMinor());
    Assertions.assertEquals(1, version.getPatch());
    Assertions.assertEquals(0, version.getBuild());
  }

  @Test
  public void constructFromStringNormal() {
    version = new Version("1.1.1.1");
    Assertions.assertEquals("1.1.1.1", version.getVersion());
    Assertions.assertEquals(1, version.getMajor());
    Assertions.assertEquals(1, version.getMinor());
    Assertions.assertEquals(1, version.getPatch());
    Assertions.assertEquals(1, version.getBuild());
  }

  @Test
  public void constructFromStringInvalidNull() {
    Assertions.assertThrows(NullPointerException.class, () -> version = new Version(null));
  }

  @Test
  public void constructFromStringInvalidEmpty() {
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> version = new Version(""));
    Assertions.assertEquals("Invalid major \"\", version \"\".", exception.getMessage());
    Assertions.assertTrue(exception.getCause() instanceof NumberFormatException);
  }

  @Test
  public void constructFromStringInvalidMajorNegative() {
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> version = new Version("-1"));
    Assertions.assertEquals("major \"-1\" can not be negative, version \"-1\".", exception.getMessage());
  }

  @Test
  public void constructFromStringInvalidMajorDot() {
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> version = new Version("1."));
    Assertions.assertEquals("Invalid minor \"\", version \"1.\".", exception.getMessage());
    Assertions.assertTrue(exception.getCause() instanceof NumberFormatException);
  }

  @Test
  public void constructFromStringInvalidMinorNegative() {
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> version = new Version("1.-1"));
    Assertions.assertEquals("minor \"-1\" can not be negative, version \"1.-1\".", exception.getMessage());
  }

  @Test
  public void constructFromStringInvalidMinorDot() {
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> version = new Version("1.1."));
    Assertions.assertEquals("Invalid patch \"\", version \"1.1.\".", exception.getMessage());
    Assertions.assertTrue(exception.getCause() instanceof NumberFormatException);
  }

  @Test
  public void constructFromStringInvalidPatchNegative() {
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> version = new Version("1.1.-1"));
    Assertions.assertEquals("patch \"-1\" can not be negative, version \"1.1.-1\".", exception.getMessage());
  }

  @Test
  public void constructFromStringInvalidPatchDot() {
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> version = new Version("1.1.1."));
    Assertions.assertEquals("Invalid build \"\", version \"1.1.1.\".", exception.getMessage());
    Assertions.assertTrue(exception.getCause() instanceof NumberFormatException);
  }

  @Test
  public void constructFromStringInvalidBuildNegative() {
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> version = new Version("1.1.1.-1"));
    Assertions.assertEquals("build \"-1\" can not be negative, version \"1.1.1.-1\".", exception.getMessage());
  }

  @Test
  public void constructFromStringInvalidTooManyPart() {
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> version = new Version("1.1.1.1."));
    Assertions.assertEquals("Invalid version \"1.1.1.1.\".", exception.getMessage());
  }

  @Test
  public void constructFromNumber() {
    version = new Version(s1, s1, s1, s1);
    Assertions.assertEquals("1.1.1.1", version.getVersion());
    Assertions.assertEquals(1, version.getMajor());
    Assertions.assertEquals(1, version.getMinor());
    Assertions.assertEquals(1, version.getPatch());
    Assertions.assertEquals(1, version.getPatch());
  }

  @Test
  public void testToString() {
    version = new Version(s1, s1, s1, s1);
    Assertions.assertEquals("1.1.1.1", version.toString());
  }

  @Test
  public void testHashCode() {
    version = new Version(s1, s1, s1, s1);
    Assertions.assertEquals(version.getVersion().hashCode(), version.hashCode());
  }

  @Test
  public void testEquals() {
    version = new Version(s1, s1, s1, s1);

    Assertions.assertEquals(version, version);
    Assertions.assertEquals(version, new Version(s1, s1, s1, s1));
    Assertions.assertNotEquals(null, version);
  }

  @Test
  public void compareTo() {
    version = new Version(s1, s1, s1, s1);

    Assertions.assertEquals(0, version.compareTo(version));
    Assertions.assertEquals(0, version.compareTo(new Version(s1, s1, s1, s1)));

    Assertions.assertEquals(-1, version.compareTo(new Version(s1, s1, s2, s1)));
    Assertions.assertEquals(-1, version.compareTo(new Version(s1, s2, s1, s1)));
    Assertions.assertEquals(-1, version.compareTo(new Version(s2, s1, s1, s1)));

    Assertions.assertEquals(1, version.compareTo(new Version((short) 0,
        Short.MAX_VALUE, Short.MAX_VALUE, Short.MAX_VALUE)));
  }
}
