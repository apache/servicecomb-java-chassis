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

import javax.management.OperationsException;
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
     * Put create value of a key
     *
     * @param key
     * @param kvBody
     * @return
     */
    public String putKeyValue(String key, KVBody kvBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            HttpResponse response = httpClient.putHttpRequest("/kie/kv/" + key, null, mapper.writeValueAsString(kvBody));
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("putKeyValue result:" + response.getContent());
                return response.getContent();
            } else {
                throw new OperationsException(response.getStatusCode() + response.getMessage() + response.getContent());
            }
        } catch (IOException | OperationsException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get get value of a key
     *
     * @param key
     * @return
     */
    public List<KVResponse> getValueOfKey(String key) {
        try {
            HttpResponse response = httpClient.getHttpRequest("/kie/kv/" + key, null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("getKeyValue result:" + response.getContent());
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.getContent(), new TypeReference<List<KVResponse>>() {
                });
            } else {
                throw new OperationsException(response.getStatusCode() + response.getMessage() + response.getContent());
            }
        } catch (IOException | OperationsException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * SearchByLabels get value by lables
     *
     * @param labels
     * @return
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
                LOGGER.info("searchKeyValue result:" + response.getContent());
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.getContent(), new TypeReference<List<KVResponse>>() {
                });
            } else {
                throw new OperationsException(response.getStatusCode() + response.getMessage() + response.getContent());
            }
        } catch (IOException | OperationsException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Delete remove kv
     *
     * @param kvDoc
     * @return
     */
    public String deleteKeyValue(KVDoc kvDoc) {
        try {
            HttpResponse response = httpClient.deleteHttpRequest("/kie/kv/?kvID=" + kvDoc.get_id(), null, null);
            if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                LOGGER.info("DeleteKeyValue OK");
                return String.format("DeleteKeyValue OK");
            } else {
                throw new OperationsException(response.getStatusCode() + response.getMessage() + response.getContent());
            }
        } catch (IOException | OperationsException e) {
            e.printStackTrace();
        }
        return null;
    }
}
