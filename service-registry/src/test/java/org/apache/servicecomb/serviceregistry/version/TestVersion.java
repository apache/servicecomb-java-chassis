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

package org.apache.servicecomb.serviceregistry.version;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestVersion {
  Version version;

  short s1 = 1;

  short s2 = 2;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void constructFromStringNormalOnlyMajor() {
    version = new Version("1");
    Assert.assertEquals("1.0.0", version.getVersion());
    Assert.assertEquals(1, version.getMajor());
    Assert.assertEquals(0, version.getMinor());
    Assert.assertEquals(0, version.getPatch());
  }

  @Test
  public void constructFromStringNormalOnlyMajorMinor() {
    version = new Version("1.1");
    Assert.assertEquals("1.1.0", version.getVersion());
    Assert.assertEquals(1, version.getMajor());
    Assert.assertEquals(1, version.getMinor());
    Assert.assertEquals(0, version.getPatch());
  }

  @Test
  public void constructFromStringNormalAll() {
    version = new Version("1.1.1");
    Assert.assertEquals("1.1.1", version.getVersion());
    Assert.assertEquals(1, version.getMajor());
    Assert.assertEquals(1, version.getMinor());
    Assert.assertEquals(1, version.getPatch());
  }

  @Test
  public void constructFromStringInvalidNull() {
    expectedException.expect(NullPointerException.class);

    version = new Version(null);
  }

  @Test
  public void constructFromStringInvalidEmpty() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers.is("Invalid major \"\", version \"\"."));
    expectedException.expectCause(Matchers.instanceOf(NumberFormatException.class));

    version = new Version("");
  }

  @Test
  public void constructFromStringInvalidMajorNegative() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers.is("major \"-1\" can not be negative, version \"-1\"."));

    version = new Version("-1");
  }

  @Test
  public void constructFromStringInvalidMajorDot() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers.is("Invalid minor \"\", version \"1.\"."));
    expectedException.expectCause(Matchers.instanceOf(NumberFormatException.class));

    version = new Version("1.");
  }

  @Test
  public void constructFromStringInvalidMinorNegative() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers.is("minor \"-1\" can not be negative, version \"1.-1\"."));

    version = new Version("1.-1");
  }

  @Test
  public void constructFromStringInvalidMinorDot() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers.is("Invalid patch \"\", version \"1.1.\"."));
    expectedException.expectCause(Matchers.instanceOf(NumberFormatException.class));

    version = new Version("1.1.");
  }

  @Test
  public void constructFromStringInvalidPatchNegative() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers.is("patch \"-1\" can not be negative, version \"1.1.-1\"."));

    version = new Version("1.1.-1");
  }

  @Test
  public void constructFromStringInvalidTooManyPart() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers.is("Invalid version \"1.1.1.\"."));

    version = new Version("1.1.1.");
  }

  @Test
  public void constructFromNumber() {
    version = new Version(s1, s1, s1);
    Assert.assertEquals("1.1.1", version.getVersion());
    Assert.assertEquals(1, version.getMajor());
    Assert.assertEquals(1, version.getMinor());
    Assert.assertEquals(1, version.getPatch());
  }

  @Test
  public void testToString() {
    version = new Version(s1, s1, s1);
    Assert.assertEquals("1.1.1", version.toString());
  }

  @Test
  public void testHashCode() {
    version = new Version(s1, s1, s1);
    Assert.assertEquals(version.getVersion().hashCode(), version.hashCode());
  }

  @Test
  public void testEquals() {
    version = new Version(s1, s1, s1);

    Assert.assertTrue(version.equals(version));
    Assert.assertTrue(version.equals(new Version(s1, s1, s1)));
    Assert.assertFalse(version.equals(null));
  }

  @Test
  public void compareTo() {
    version = new Version(s1, s1, s1);

    Assert.assertEquals(0, version.compareTo(version));
    Assert.assertEquals(0, version.compareTo(new Version(s1, s1, s1)));

    Assert.assertEquals(-1, version.compareTo(new Version(s1, s1, s2)));
    Assert.assertEquals(-1, version.compareTo(new Version(s1, s2, s1)));
    Assert.assertEquals(-1, version.compareTo(new Version(s2, s1, s1)));

    Assert.assertEquals(1, version.compareTo(new Version((short) 0, Short.MAX_VALUE, Short.MAX_VALUE)));
  }
}
