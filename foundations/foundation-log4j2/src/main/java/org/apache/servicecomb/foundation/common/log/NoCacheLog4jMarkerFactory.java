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

import org.apache.logging.slf4j.Log4jMarkerFactory;
import org.slf4j.Marker;

/**
 * avoid log4j2 leak marker instances
 */
public class NoCacheLog4jMarkerFactory extends Log4jMarkerFactory {
  @Override
  public Marker getMarker(Marker slf4jMarker) {
    if (slf4jMarker instanceof NoCacheMarker) {
      return new NoCacheLog4j2Marker(slf4jMarker);
    }

    return super.getMarker(slf4jMarker);
  }
}
