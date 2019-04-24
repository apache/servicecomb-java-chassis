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
package org.apache.servicecomb.foundation.common.log;

import org.apache.logging.log4j.Marker;
import org.apache.logging.slf4j.Log4jMarker;

/**
 * avoid log4j2 leak marker instances
 */
public class NoCacheLog4j2Marker extends Log4jMarker implements Marker {
  private static final long serialVersionUID = -1L;

  private final org.slf4j.Marker slf4jMarker;

  public NoCacheLog4j2Marker(org.slf4j.Marker slf4jMarker) {
    super(null);
    this.slf4jMarker = slf4jMarker;
  }

  @Override
  public Marker getLog4jMarker() {
    return this;
  }

  @Override
  public Marker addParents(Marker... markers) {
    return this;
  }

  @Override
  public final String getName() {
    return slf4jMarker.getName();
  }

  @Override
  public Marker[] getParents() {
    return null;
  }

  @Override
  public boolean hasParents() {
    return false;
  }

  @Override
  public boolean isInstanceOf(Marker m) {
    return false;
  }

  @Override
  public boolean isInstanceOf(String name) {
    return false;
  }

  @Override
  public boolean remove(Marker marker) {
    return false;
  }

  @Override
  public Marker setParents(Marker... markers) {
    return this;
  }

  @Override
  public String toString() {
    return slf4jMarker.getName();
  }
}
