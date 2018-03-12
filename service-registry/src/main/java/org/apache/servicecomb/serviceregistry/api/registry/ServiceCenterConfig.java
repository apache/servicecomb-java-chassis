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

package org.apache.servicecomb.serviceregistry.api.registry;

public class ServiceCenterConfig {
  private int maxHeaderBytes;

  private int maxBodyBytes;

  private String readHeaderTimeout;

  private String readTimeout;

  private String idleTimeout;

  private String writeTimeout;

  private String limitTTLUnit;

  private int limitConnections;

  private String limitIPLookup;

  private String sslEnabled;

  private String sslMinVersion;

  private String sslVerifyPeer;

  private String sslCiphers;

  private String autoSyncInterval;

  private int compactIndexDelta;

  private String compactInterval;

  private int logRotateSize;

  private int logBackupCount;

  public int getMaxHeaderBytes() {
    return maxHeaderBytes;
  }

  public void setMaxHeaderBytes(int maxHeaderBytes) {
    this.maxHeaderBytes = maxHeaderBytes;
  }

  public int getMaxBodyBytes() {
    return maxBodyBytes;
  }

  public void setMaxBodyBytes(int maxBodyBytes) {
    this.maxBodyBytes = maxBodyBytes;
  }

  public String getReadHeaderTimeout() {
    return readHeaderTimeout;
  }

  public void setReadHeaderTimeout(String readHeaderTimeout) {
    this.readHeaderTimeout = readHeaderTimeout;
  }

  public String getReadTimeout() {
    return readTimeout;
  }

  public void setReadTimeout(String readTimeout) {
    this.readTimeout = readTimeout;
  }

  public String getIdleTimeout() {
    return idleTimeout;
  }

  public void setIdleTimeout(String idleTimeout) {
    this.idleTimeout = idleTimeout;
  }

  public String getWriteTimeout() {
    return writeTimeout;
  }

  public void setWriteTimeout(String writeTimeout) {
    this.writeTimeout = writeTimeout;
  }

  public String getLimitTTLUnit() {
    return limitTTLUnit;
  }

  public void setLimitTTLUnit(String limitTTLUnit) {
    this.limitTTLUnit = limitTTLUnit;
  }

  public int getLimitConnections() {
    return limitConnections;
  }

  public void setLimitConnections(int limitConnections) {
    this.limitConnections = limitConnections;
  }

  public String getLimitIPLookup() {
    return limitIPLookup;
  }

  public void setLimitIPLookup(String limitIPLookup) {
    this.limitIPLookup = limitIPLookup;
  }

  public String getSslEnabled() {
    return sslEnabled;
  }

  public void setSslEnabled(String sslEnabled) {
    this.sslEnabled = sslEnabled;
  }

  public String getSslMinVersion() {
    return sslMinVersion;
  }

  public void setSslMinVersion(String sslMinVersion) {
    this.sslMinVersion = sslMinVersion;
  }

  public String getSslVerifyPeer() {
    return sslVerifyPeer;
  }

  public void setSslVerifyPeer(String sslVerifyPeer) {
    this.sslVerifyPeer = sslVerifyPeer;
  }

  public String getSslCiphers() {
    return sslCiphers;
  }

  public void setSslCiphers(String sslCiphers) {
    this.sslCiphers = sslCiphers;
  }

  public String getAutoSyncInterval() {
    return autoSyncInterval;
  }

  public void setAutoSyncInterval(String autoSyncInterval) {
    this.autoSyncInterval = autoSyncInterval;
  }

  public int getCompactIndexDelta() {
    return compactIndexDelta;
  }

  public void setCompactIndexDelta(int compactIndexDelta) {
    this.compactIndexDelta = compactIndexDelta;
  }

  public String getCompactInterval() {
    return compactInterval;
  }

  public void setCompactInterval(String compactInterval) {
    this.compactInterval = compactInterval;
  }

  public int getLogRotateSize() {
    return logRotateSize;
  }

  public void setLogRotateSize(int logRotateSize) {
    this.logRotateSize = logRotateSize;
  }

  public int getLogBackupCount() {
    return logBackupCount;
  }

  public void setLogBackupCount(int logBackupCount) {
    this.logBackupCount = logBackupCount;
  }
}
