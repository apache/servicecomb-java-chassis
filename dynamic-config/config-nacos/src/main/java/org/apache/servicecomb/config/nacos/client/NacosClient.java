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

package org.apache.servicecomb.config.nacos.client;

import static org.apache.servicecomb.config.nacos.client.ConfigurationAction.CREATE;
import static org.apache.servicecomb.config.nacos.client.ConfigurationAction.DELETE;
import static org.apache.servicecomb.config.nacos.client.ConfigurationAction.SET;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;

import org.apache.servicecomb.config.YAMLUtil;
import org.apache.servicecomb.config.nacos.archaius.sources.NacosConfigurationSourceImpl.UpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class NacosClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosClient.class);

    private static final NacosConfig NACOS_CONFIG = NacosConfig.INSTANCE;

    private static final Map<String, Object> originalConfigMap = new ConcurrentHashMap<>();

    private final String serverAddr = NACOS_CONFIG.getServerAddr();

    private final String dataId = NACOS_CONFIG.getDataId();

    private final String group = NACOS_CONFIG.getGroup();

    private final String namespace = NACOS_CONFIG.getNamespace();

    private final String fileExtension = NACOS_CONFIG.getFileExtension();

    private final UpdateHandler updateHandler;

    public NacosClient(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    public void refreshNacosConfig() {
        new ConfigRefresh(serverAddr, dataId, group,namespace,fileExtension).refreshConfig();
    }

    class ConfigRefresh {
        private final String serverAddr;

        private final String dataId;

        private final String group;

        private final String namespace;

        private final String fileExtension;
        /**
         * JSON 类型的字符串转换成 Map
         */
        void parseJSON2Map(Map jsonMap,String jsonStr,String parentKey){
            //字符串转换成JSON对象
            JSONObject json = JSONObject.parseObject(jsonStr);
            //最外层JSON解析
            for(Object k : json.keySet()){
                //JSONObject 实际上相当于一个Map集合，所以我们可以通过Key值获取Value
                Object v = json.get(k);
                //构造一个包含上层keyName的完整keyName
                String fullKey = (null == parentKey || parentKey.trim().equals("") ? k.toString() : parentKey + "." + k);

                if(v instanceof JSONArray){
                    //如果内层还是数组的话，继续解析
                    Iterator it = ((JSONArray) v).iterator();
                    while(it.hasNext()){
                        JSONObject json2 = (JSONObject)it.next();
                        parseJSON2Map(jsonMap,json2.toString(),fullKey);
                    }
                } else if(v instanceof JSONObject){
                    parseJSON2Map(jsonMap,v.toString(),fullKey);
                }
                else{
                    jsonMap.put(fullKey, v);
                }
            }
        }

        void parseConfigInfo(String config){
             try {
                 Map<String, Object> body = new LinkedHashMap<>();
                 String extension= StringUtils.isEmpty(fileExtension)?dataId.substring(dataId.lastIndexOf(".")+1):fileExtension;
                 switch (extension) {
                     case "yaml":
                         InputStream inputStream = new ByteArrayInputStream(config.getBytes());
                         body = YAMLUtil.yaml2Properties(inputStream);
                         break;
                     case "json":
                         parseJSON2Map(body, config, null);
                         break;
                     case "properties":
                         BufferedReader br = new BufferedReader(new StringReader(config));
                         String line = null;
                         while((line = br.readLine())!=null){
                             String[] kv= line.split("=");
                             body.put(kv[0].trim(),kv[1].trim());
                         }
                         br.close();
                         break;
                 }

                 refreshConfigItems(body);
             }
             catch (IOException e){
                 LOGGER.error("Parsing error for nacos config :" + e.getMessage());
             }
        }
        ConfigRefresh(String serverAddr, String dataId, String group,String namespace,String fileExtension) {
            this.serverAddr = serverAddr;
            this.dataId = dataId;
            this.group = group;
            this.namespace=namespace;
            this.fileExtension=fileExtension;
        }

        @SuppressWarnings("unchecked")
        void refreshConfig() {
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            properties.put("dataId", dataId);
            properties.put("group", group);
            properties.put("namespace", namespace);
            try {
                ConfigService configService = NacosFactory.createConfigService(properties);
                String content = configService.getConfig(dataId, group, 5000);

                parseConfigInfo(content);
                configService.addListener(dataId, group, new Listener() {
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        LOGGER.info("receive from nacos:" + configInfo);
                        try {
                            parseConfigInfo(configInfo);
                        } catch (Exception e) {
                            LOGGER.error("JsonObject parse config center response error: ", e);
                        }
                    }

                    @Override
                    public Executor getExecutor() {
                        return null;
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Receive nacos config error: ", e);
            }
        }

        private void refreshConfigItems(Map<String, Object> map) {
            compareChangedConfig(originalConfigMap, map);
            originalConfigMap.clear();
            originalConfigMap.putAll(map);
        }

        void compareChangedConfig(Map<String, Object> before, Map<String, Object> after) {
            Map<String, Object> itemsCreated = new HashMap<>();
            Map<String, Object> itemsDeleted = new HashMap<>();
            Map<String, Object> itemsModified = new HashMap<>();
            if (before == null || before.isEmpty()) {
                updateHandler.handle(CREATE, after);
                return;
            }
            if (after == null || after.isEmpty()) {
                updateHandler.handle(DELETE, before);
                return;
            }
            after.entrySet().forEach(stringObjectEntry -> {
                String itemKey = stringObjectEntry.getKey();
                Object itemValue = stringObjectEntry.getValue();
                if (!before.containsKey(itemKey)) {
                    itemsCreated.put(itemKey, itemValue);
                } else if (!itemValue.equals(before.get(itemKey))) {
                    itemsModified.put(itemKey, itemValue);
                }
            });
            for (String itemKey : before.keySet()) {
                if (!after.containsKey(itemKey)) {
                    itemsDeleted.put(itemKey, "");
                }
            }
            updateHandler.handle(CREATE, itemsCreated);
            updateHandler.handle(SET, itemsModified);
            updateHandler.handle(DELETE, itemsDeleted);
        }
    }
}

