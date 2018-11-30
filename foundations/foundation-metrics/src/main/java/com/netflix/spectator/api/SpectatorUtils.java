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
package com.netflix.spectator.api;

public final class SpectatorUtils {
  private SpectatorUtils() {
  }

  public static Id createDefaultId(String name) {
    return new DefaultId(name);
  }

  public static void removeExpiredMeters(Registry registry) {
    if (registry instanceof AbstractRegistry) {
      ((AbstractRegistry) registry).removeExpiredMeters();
    }
  }

  public static void registerMeter(Registry registry, Meter meter) {
    if (!(registry instanceof AbstractRegistry)) {
      throw new IllegalStateException("registry must be a AbstractRegistry, class=" + registry.getClass().getName());
    }
    ((AbstractRegistry) registry).getOrCreate(meter.id(), Meter.class, null, _id -> meter);
  }
}
