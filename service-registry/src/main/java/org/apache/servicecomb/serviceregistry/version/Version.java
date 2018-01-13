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

import java.util.Objects;

import org.apache.commons.lang.ArrayUtils;

// short version is enough
public class Version implements Comparable<Version> {
  private static final String[] ZERO = new String[] {"0", "0", "0"};

  private final short major;

  private final short minor;

  private final short patch;

  private final String version;

  private final long numberVersion;

  // 1
  // 1.0
  // 1.0.0
  public Version(String version) {
    Objects.requireNonNull(version);

    String[] versions = version.split("\\.", -1);
    if (versions.length > 3) {
      throw new IllegalStateException(String.format("Invalid version \"%s\".", version));
    }

    if (versions.length < 3) {
      versions = (String[]) ArrayUtils.addAll(versions, ZERO);
    }
    this.major = parseNumber("major", version, versions[0]);
    this.minor = parseNumber("minor", version, versions[1]);
    this.patch = parseNumber("patch", version, versions[2]);

    this.version = combineStringVersion();
    this.numberVersion = combineVersion();
  }

  private short parseNumber(String name, String allVersion, String version) {
    short value = 0;
    try {
      value = Short.parseShort(version);
    } catch (NumberFormatException e) {
      throw new IllegalStateException(String.format("Invalid %s \"%s\", version \"%s\".", name, version, allVersion),
          e);
    }

    if (value < 0) {
      throw new IllegalStateException(
          String.format("%s \"%s\" can not be negative, version \"%s\".", name, version, allVersion));
    }

    return value;
  }

  public Version(short major, short minor, short patch) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
    this.version = combineStringVersion();
    this.numberVersion = combineVersion();
  }

  private String combineStringVersion() {
    return major + "." + minor + "." + patch;
  }

  private long combineVersion() {
    return (long) major << 32 | (long) minor << 16 | (long) patch;
  }

  public short getMajor() {
    return major;
  }

  public short getMinor() {
    return minor;
  }

  public short getPatch() {
    return patch;
  }

  public String getVersion() {
    return version;
  }

  @Override
  public String toString() {
    return version;
  }

  @Override
  public int hashCode() {
    return version.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }

    if (!(other instanceof Version)) {
      return false;
    }

    return numberVersion == ((Version) other).numberVersion;
  }

  @Override
  public int compareTo(Version other) {
    return Long.compare(numberVersion, other.numberVersion);
  }
}
