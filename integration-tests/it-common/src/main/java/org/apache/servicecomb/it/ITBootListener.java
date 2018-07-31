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
package org.apache.servicecomb.it;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.apache.servicecomb.core.BootListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicPropertyFactory;

@Component
public class ITBootListener implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ITBootListener.class);

  private static boolean done;

  public static boolean isDone() {
    return done;
  }

  @Override
  public void onBootEvent(BootEvent event) {
    if (!EventType.BEFORE_HANDLER.equals(event.getEventType())) {
      return;
    }

    selectPort("servicecomb.rest.address");
    selectPort("servicecomb.highway.address");
    done = true;
  }

  protected void selectPort(String cfgKey) {
    String endpoint = DynamicPropertyFactory.getInstance().getStringProperty(cfgKey, null).get();
    if (endpoint == null) {
      return;
    }

    URI uri = URI.create("http://" + endpoint);
    if (uri.getPort() == 0) {
      int port = getRandomPort();
      try {
        ConcurrentCompositeConfiguration config = (ConcurrentCompositeConfiguration) DynamicPropertyFactory
            .getBackingConfigurationSource();
        endpoint = new URIBuilder("http://" + endpoint).setPort(port).build().toString().substring(7);
        config.getConfiguration(0).setProperty(cfgKey, endpoint);
        LOGGER.info("change {} to {}.", cfgKey, endpoint);
      } catch (URISyntaxException e) {
        throw new IllegalStateException("Failed to build endpoint.", e);
      }
    }
  }

  protected int getRandomPort() {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      return serverSocket.getLocalPort();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to get random port.", e);
    }
  }
}
