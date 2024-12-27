package org.apache.servicecomb.registry.consul.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class HostInfo {

  private boolean override;

  private String ipAddress;

  private String hostname;

  public HostInfo(String hostname) {
    this.hostname = hostname;
  }

  public HostInfo() {
  }

  public int getIpAddressAsInt() {
    InetAddress inetAddress = null;
    String host = this.ipAddress;
    if (host == null) {
      host = this.hostname;
    }
    try {
      inetAddress = InetAddress.getByName(host);
    } catch (final UnknownHostException e) {
      throw new IllegalArgumentException(e);
    }
    return ByteBuffer.wrap(inetAddress.getAddress()).getInt();
  }

  public boolean isOverride() {
    return this.override;
  }

  public void setOverride(boolean override) {
    this.override = override;
  }

  public String getIpAddress() {
    return this.ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getHostname() {
    return this.hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }
}
