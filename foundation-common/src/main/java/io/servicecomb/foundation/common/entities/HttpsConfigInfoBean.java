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
package io.servicecomb.foundation.common.entities;

public class HttpsConfigInfoBean {
    private String keyStorePath = null;

    private String keyStorePasswd = null;

    private String trustStorePath = null;

    private String trustStorePasswd = null;

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getKeyStorePasswd() {
        return keyStorePasswd;
    }

    public void setKeyStorePasswd(String keyStorePasswd) {
        this.keyStorePasswd = keyStorePasswd;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public String getTrustStorePasswd() {
        return trustStorePasswd;
    }

    public void setTrustStorePasswd(String trustStorePasswd) {
        this.trustStorePasswd = trustStorePasswd;
    }

}
