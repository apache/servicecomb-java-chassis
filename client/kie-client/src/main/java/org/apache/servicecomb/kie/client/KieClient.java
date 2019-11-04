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

package org.apache.servicecomb.kie.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.apache.servicecomb.kie.client.http.HttpResponse;
import org.apache.servicecomb.kie.client.model.KVBody;
import org.apache.servicecomb.kie.client.model.KVDoc;
import org.apache.servicecomb.kie.client.model.KVResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by   on 2019/10/28.
 */
public class KieClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieClient.class);

    private KieRawClient httpClient;

    public KieClient() {
        this(new KieRawClient());
    }

    /**
     * Customized host,port,domainName and if any one parameter is null, it will be defaults
     *
     * @param host
     * @param port
     * @param domainName
     */
    public KieClient(String host, int port, String domainName) {
        this.httpClient = new KieRawClient.Builder().setHost(host).setPort(port).setDomainName(domainName).build();
    }

    public KieClient(KieRawClient serviceCenterRawClient) {
        this.httpClient = serviceCenterRawClient;
    }

    /**
     * Create value of a key
     *
     * @param key
     * @param kvBody
     * @return key-value json string; when some error happens, return null
     */
    public String putKeyValue(String key, KVBody kvBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            HttpResponse response = httpClient.putHttpRequest("/kie/kv/" + key, null, mapper.writeValueAsString(kvBody));
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                return response.getContent();
            } else {
                LOGGER.error("create keyValue fails, responseStatusCode={}, responseMessage={}, responseContent{}",response.getStatusCode(), response.getMessage(), response.getContent());
            }
        } catch (IOException e) {
            LOGGER.error("create keyValue fails",e);
        }
        return null;
    }

    /**
     * Get value of a key
     *
     * @param key
     * @return List<KVResponse>; when some error happens, return null
     */
    public List<KVResponse> getValueOfKey(String key) {
        try {
            HttpResponse response = httpClient.getHttpRequest("/kie/kv/" + key, null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.getContent(), new TypeReference<List<KVResponse>>() {
                });
            } else {
                LOGGER.error("get value of key fails, responseStatusCode={}, responseMessage={}, responseContent{}",response.getStatusCode(), response.getMessage(), response.getContent());
            }
        } catch (IOException e) {
            LOGGER.error("get value of key fails",e);
        }
        return null;
    }

    /**
     * SearchByLabels get value by lables
     *
     * @param labels
     * @return List<KVResponse>; when some error happens, return null
     */
    public List<KVResponse> searchKeyValueByLabels(Map<String, String> labels) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (Entry<String, String> entry : labels.entrySet()) {
                stringBuilder.append(entry.getKey());
                stringBuilder.append(":");
                stringBuilder.append(entry.getValue());
                stringBuilder.append("+");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            HttpResponse response = httpClient.getHttpRequest("/kie/kv?q=" + stringBuilder.toString(), null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.getContent(), new TypeReference<List<KVResponse>>() {
                });
            } else {
                LOGGER.error("search keyValue by labels fails, responseStatusCode={}, responseMessage={}, responseContent{}",response.getStatusCode(), response.getMessage(), response.getContent());
            }
        } catch (IOException e) {
            LOGGER.error("search keyValue by labels fails",e);
        }
        return null;
    }

    /**
     * Delete remove kv
     *
     * @param kvDoc
     * @return void
     */
    public void deleteKeyValue(KVDoc kvDoc) {
        try {
            HttpResponse response = httpClient.deleteHttpRequest("/kie/kv/?kvID=" + kvDoc.getId(), null, null);
            if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                LOGGER.info("Delete keyValue success");
            } else {
                LOGGER.error("delete keyValue fails, responseStatusCode={}, responseMessage={}, responseContent{}",response.getStatusCode(), response.getMessage(), response.getContent());
            }
        } catch (IOException e) {
            LOGGER.error("delete keyValue fails",e);
        }
    }
}
