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

package org.apache.servicecomb.core;

public class Config {
  private static int clientThread;

  private static String transport;

  private static String mode;

  public static String getTransport() {
    return transport;
  }

  public static void setTransport(String transport) {
    Config.transport = transport;
  }

  public static int getClientThread() {
    return clientThread;
  }

  public static void setClientThread(int clientThread) {
    Config.clientThread = clientThread;
  }

  public static String getMode() {
    return mode;
  }

  public static void setMode(String mode) {
    Config.mode = mode;
  }
}
