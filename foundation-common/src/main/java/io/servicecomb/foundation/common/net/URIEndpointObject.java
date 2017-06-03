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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class URIEndpointObject extends IpPort {
    private static final String SSL_ENABLED_KEY = "sslEnabled";

    private boolean sslEnabled;

    private Map<String, List<String>> querys;

    public URIEndpointObject(String endpoint) {
        URI uri = URI.create(endpoint);
        setHostOrIp(uri.getHost());
        if (uri.getPort() < 0) {
            // do not use default port
            throw new IllegalArgumentException("port not specified.");
        }
        setPort(uri.getPort());
        querys = splitQuery(uri);
        if (querys.get(SSL_ENABLED_KEY) != null && querys.get(SSL_ENABLED_KEY).size() > 0) {
            sslEnabled = Boolean.parseBoolean(querys.get(SSL_ENABLED_KEY).get(0));
        } else {
            sslEnabled = false;
        }
    }

    public static Map<String, List<String>> splitQuery(URI uri) {
        final Map<String, List<String>> queryPairs = new LinkedHashMap<String, List<String>>();
        try {
            String query = uri.getQuery();
            if (query == null || query.isEmpty()) {
                return queryPairs;
            }
            final String[] pairs = query.split("&");
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key =
                    idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name()) : pair;
                if (!queryPairs.containsKey(key)) {
                    queryPairs.put(key, new LinkedList<String>());
                }
                final String value =
                    idx > 0 && pair.length() > idx + 1
                            ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name()) : null;
                queryPairs.get(key).add(value);
            }
            return queryPairs;
        } catch (UnsupportedEncodingException e) {
            return queryPairs;
        }
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public List<String> getQuery(String key) {
        return querys.get(key);
    }

    public String getFirst(String key) {
        List<String> values = querys.get(key);
        if (values == null || values.isEmpty()) {
            return null;
        }

        return values.get(0);
    }
}
