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

package org.apache.servicecomb.service.center.client.http;

public class TLSConfig {

    //keyStore type
    public enum KeyStoreInstanceType {
        JKS, JCEKS, PKCS12, PKCS11, DKS
    }

    private KeyStoreInstanceType keyStoreType;

    //trust certificate
    private String trustStore;

    //trust certificate password
    private String trustStoreValue;

    //identity certificate
    private String keyStore;

    //identity certificate password
    private String keyStoreValue;

    public TLSConfig(){

    }

    public TLSConfig(KeyStoreInstanceType keyStoreType, String trustStore, String trustStoreValue, String keyStore, String keyStoreValue) {
        this.keyStoreType = keyStoreType;
        this.trustStore = trustStore;
        this.trustStoreValue = trustStoreValue;
        this.keyStore = keyStore;
        this.keyStoreValue = keyStoreValue;
    }

    public KeyStoreInstanceType getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(KeyStoreInstanceType keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStoreValue() {
        return trustStoreValue;
    }

    public void setTrustStoreValue(String trustStoreValue) {
        this.trustStoreValue = trustStoreValue;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getKeyStoreValue() {
        return keyStoreValue;
    }

    public void setKeyStoreValue(String keyStoreValue) {
        this.keyStoreValue = keyStoreValue;
    }
}
