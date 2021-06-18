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

package org.apache.servicecomb.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.DynamicPropertyUpdater;
import com.netflix.config.DynamicWatchedConfiguration;
import com.netflix.config.WatchedConfigurationSource;
import com.netflix.config.WatchedUpdateListener;
import com.netflix.config.WatchedUpdateResult;

/**
 * Same as DynamicWatchedConfiguration but Disable delimiter parsing for string
 *
 * @see DynamicWatchedConfiguration
 */
@SuppressWarnings("unchecked")
public class DynamicWatchedConfigurationExt extends ConcurrentMapConfiguration implements WatchedUpdateListener {

  private final boolean ignoreDeletesFromSource;

  private final DynamicPropertyUpdater updater;

  private static final Logger LOGGER = LoggerFactory.getLogger(DynamicWatchedConfigurationExt.class);

  private DynamicWatchedConfigurationExt(WatchedConfigurationSource source, boolean ignoreDeletesFromSource,
      DynamicPropertyUpdater updater) {
    this.ignoreDeletesFromSource = ignoreDeletesFromSource;
    this.updater = updater;

    setDelimiterParsingDisabled(true);

    // get a current snapshot of the config source data
    try {
      Map<String, Object> currentData = source.getCurrentData();
      WatchedUpdateResult result = WatchedUpdateResult.createFull(currentData);

      updateConfiguration(result);
    } catch (final Exception exc) {
      LOGGER.error("could not getCurrentData() from the WatchedConfigurationSource", exc);
    }

    // add a listener for subsequent config updates
    source.addUpdateListener(this);
  }

  public DynamicWatchedConfigurationExt(final WatchedConfigurationSource source) {
    this(source, false, new DynamicPropertyUpdater());
  }

  @Override
  public void updateConfiguration(final WatchedUpdateResult result) {
    updater.updateProperties(result, this, ignoreDeletesFromSource);
  }
}