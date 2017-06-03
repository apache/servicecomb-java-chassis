/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.common.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NetUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetUtils.class);

    private static Map<String, String> allHostAddresses = new HashMap<>();

    private static String hostName;

    private static String hostAddress;

    static {
        try {
            doGetIpv4AddressFromNetworkInterface();
            // this will throw exception in some docker image
            hostName = InetAddress.getLocalHost().getHostName();
            hostAddress = InetAddress.getLocalHost().getHostAddress();
            allHostAddresses.put(hostName, hostAddress);
            LOGGER.info(
                    "add hostName:" + hostName + ",hostAddress:" + hostAddress);
        } catch (Exception e) {
            LOGGER.error("got exception when trying to get addresses: {}", e);
        }

    }

    private NetUtils() {
    }

    /**
     * docker环境中，有时无法通过InetAddress.getLocalHost()获取 ，会报unknown host Exception， system error
     * 此时，通过遍历网卡接口的方式规避，出来的数据不一定对
     * @throws SocketException
     */
    private static void doGetIpv4AddressFromNetworkInterface() throws SocketException {
        Enumeration<NetworkInterface> iterNetwork = NetworkInterface.getNetworkInterfaces();

        while (iterNetwork.hasMoreElements()) {
            NetworkInterface network = iterNetwork.nextElement();

            if (!network.isUp() || network.isLoopback() || network.isVirtual()) {
                continue;
            }

            Enumeration<InetAddress> iterAddress = network.getInetAddresses();
            while (iterAddress.hasMoreElements()) {
                InetAddress address = iterAddress.nextElement();

                if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isMulticastAddress()
                        || Inet6Address.class.isInstance(address)) {
                    continue;
                }

                if (Inet4Address.class.isInstance(address)) {
                    String host = address.getHostName();
                    if (host == null) {
                        host = network.getName();
                    }
                    hostName = host;
                    hostAddress = address.getHostAddress();
                    LOGGER.info(
                            "add hostName:" + host + ",hostAddress:" + address.getHostAddress());
                    allHostAddresses.put(address.getHostName(), address.getHostAddress());
                }
            }
        }

        return;
    }

    public static IpPort parseIpPort(String address) {
        if (address == null) {
            return null;
        }

        int idx = address.indexOf(':');
        if (idx == -1) {
            return null;
        }
        String hostOrIp = address.substring(0, idx);
        int port = Integer.parseInt(address.substring(idx + 1));

        return new IpPort(hostOrIp, port);
    }

    /**
     * 将URI转换为IpPort
     * @param uriAddress
     * @return IpPort
     */
    public static IpPort parseIpPortFromURI(String uriAddress) {
        if (uriAddress == null) {
            return null;
        }

        try {
            URI uri = new URI(uriAddress);
            String authority = uri.getAuthority();
            return parseIpPort(authority);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * 对于配置为0.0.0.0的地址，let it go
     * @param schema schema, e.g. http
     * @param address adddress, e.g 0.0.0.0:8080
     * @return 实际监听的地址
     */
    public static String getRealListenAddress(String schema, String address) {
        if (address == null) {
            return address;
        }
        try {
            URI originalURI = new URI(schema + "://" + address);
            IpPort ipPort = NetUtils.parseIpPort(originalURI.getAuthority());
            if (ipPort == null) {
                LOGGER.warn("address {} not valid.", address);
                return null;
            }
            return originalURI.toString();
        } catch (URISyntaxException e) {
            LOGGER.warn("address {} not valid.", address);
            return null;
        }
    }


    public static String getHostName() {
        return hostName;
    }

    public static String getHostAddress() {
        return hostAddress;
    }

    public static String getHostAddress(String interfaceName) {
        return allHostAddresses.get(interfaceName);
    }
}
