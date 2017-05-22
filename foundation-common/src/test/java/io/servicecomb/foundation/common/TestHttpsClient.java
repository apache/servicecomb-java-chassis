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

package io.servicecomb.foundation.common;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.foundation.common.entities.HttpsConfigInfoBean;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestHttpsClient {

    /**
     * Create default Client and test requests
     */
    @Test
    public void testHttpClient() throws ClientProtocolException, IOException {

        // test valid Invalid Inputs
        Map<String, String> oHeaders = new HashMap<String, String>();
        oHeaders.put("X-Auth", "JHGUGJGH");
        HttpResponse oResponse = HttpsClient.execute("UNKNOWN METHOD", oHeaders, "//check", "body", null);
        Assert.assertEquals(null, oResponse);
        oResponse = HttpsClient.execute("", oHeaders, "//check", "body", null);
        Assert.assertEquals(null, oResponse);
        oResponse = HttpsClient.execute("UNKNOWN METHOD", oHeaders, "", "body", null);
        Assert.assertEquals(null, oResponse);
        oResponse = HttpsClient.execute("UNKNOWN METHOD", null, "//check", "body", null);
        Assert.assertEquals(null, oResponse);
        // With default Bean
        HttpsConfigInfoBean oBean = new HttpsConfigInfoBean();
        oBean.setKeyStorePath("JHGJ");
        oBean.setKeyStorePasswd("HJGJH");
        oBean.setTrustStorePasswd("JHGJHG");
        oBean.setTrustStorePath("JHGJGj");
        Assert.assertEquals("JHGJGj", oBean.getTrustStorePath());
        Assert.assertEquals("JHGJHG", oBean.getTrustStorePasswd());
        oResponse = HttpsClient.execute("UNKNOWN METHOD", oHeaders, "//check", "body", oBean);
        Assert.assertEquals(null, oResponse);

        HttpsClient.getHttpsClient(oBean);
        Assert.assertNotEquals(null, HttpsClient.getHttpsClient(Mockito.mock(HttpsConfigInfoBean.class)));
        //Handle Error Scenarios
        try {
            oResponse = HttpsClient.execute("POST", oHeaders, "//check", "body", oBean);
        } catch (Exception e) {
            Assert.assertEquals("Target host is null", e.getMessage());
        }

        try {
            oResponse = HttpsClient.execute("GET", oHeaders, "//check", "body", oBean);
        } catch (Exception e) {
            Assert.assertEquals("Target host is null", e.getMessage());
        }

        try {
            oResponse = HttpsClient.execute("DELETE", oHeaders, "//check", "body", oBean);

        } catch (Exception e) {
            Assert.assertEquals("Target host is null", e.getMessage());
        }
        // TODO Check the valid Responses
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRegisterManager() throws Error {
        // Check Name
        @SuppressWarnings("rawtypes")
        RegisterManager oManager = new RegisterManager("test");
        Assert.assertEquals("test", oManager.getName());
        // Check the Formats
        oManager.setRegisterErrorFmt("ErrorFormats");
        Assert.assertEquals("ErrorFormats", oManager.getRegisterErrorFmt());
        // Check the Registrations
        oManager.register("test1", "value1");
        Assert.assertEquals("value1", oManager.findValue("test1"));
        Assert.assertEquals("value1", oManager.ensureFindValue("test1"));
        Assert.assertEquals(1, oManager.keys().size());
        Assert.assertEquals(1, oManager.values().size());
        try {
            oManager.register("test1", "value1");

        } catch (Throwable e) {
            Assert.assertEquals(true, (e.getMessage()).contains("ErrorFormats"));
        }
        try {
            oManager.ensureFindValue("test133");

        } catch (Throwable e) {
            Assert.assertEquals(true, (e.getMessage()).contains("Can not find value"));
        }

    }

    /**
     * Test AbstractObjectManager
     */
    @Test
    public void testAbstractObjectManager() {
        AbstractObjectManager<String, String, String> oAbstractObjectManager =
            new AbstractObjectManager<String, String, String>() {

                @Override
                protected String getKey(String keyOwner) {
                    // TODO Auto-generated method stub
                    return keyOwner;
                }

                @Override
                protected String create(String keyOwner) {
                    // this.objMap.put(keyOwner, "testValue");
                    // TODO Auto-generated method stub
                    return keyOwner;
                }
            };
        Assert.assertEquals("test", oAbstractObjectManager.getOrCreate("test"));
        Assert.assertNotEquals(null, oAbstractObjectManager.keys());
        Assert.assertNotEquals(null, oAbstractObjectManager.values());
        Assert.assertEquals("test", oAbstractObjectManager.findByKey("test"));
        Assert.assertEquals("test", oAbstractObjectManager.findByContainer("test"));
    }

    /**
     * Test CommonThread
     */
    @Test
    public void testCommonThread() {
        CommonThread oThread = new CommonThread();

        oThread = new CommonThread("test", 12);
        Assert.assertEquals(true, oThread.isRunning());
        Assert.assertEquals(false, oThread.isShutdown());
    }

    /**
     * Test NamedThreadFactory
     */
    @Test
    public void testNamedThreadFactory() {
        NamedThreadFactory oNamedThreadFactory = new NamedThreadFactory();
        oNamedThreadFactory.setPrefix("test");
        Assert.assertEquals("test", oNamedThreadFactory.getPrefix());
        oNamedThreadFactory = new NamedThreadFactory("testAutoPrefix");
        Assert.assertEquals("testAutoPrefix", oNamedThreadFactory.getPrefix());
        Assert.assertNotEquals(null, oNamedThreadFactory.newThread(new Runnable() {

            @Override
            public void run() {

            }
        }));

    }

    @Test
    public void testFullOperation() {

        new MockUp<KeyStore>() {

            @Mock
            public KeyStore getInstance(String type) throws KeyStoreException {
                throw new KeyStoreException();

            }

        };

        HttpsClient.getHttpsClient();

        new MockUp<KeyStore>() {

            @Mock
            public KeyStore getInstance(String type) throws KeyStoreException {
                throw new RuntimeException();

            }

        };

        HttpsClient.getHttpsClient();

        HttpsConfigInfoBean oBean = new HttpsConfigInfoBean();
        HttpsClient.getHttpsClient(oBean);
        Assert.assertNotEquals(null, HttpsClient.getHttpsClient(Mockito.mock(HttpsConfigInfoBean.class)));
    }

    @Test
    public void testGeneralSecurityException() {

        new MockUp<HttpsClient>() {

            @Mock
            private SSLContext createSSLContext(
                    HttpsConfigInfoBean configBean) throws GeneralSecurityException, IOException {
                throw new GeneralSecurityException();

            }
        };

        HttpsConfigInfoBean oBean = new HttpsConfigInfoBean();
        Assert.assertNotNull(HttpsClient.getHttpsClient(oBean));
    }

    @Test
    public void testCreateSSLContext() {

        final String SSL_VERSION = "TLSv1.2";

        String sslVersion = SSL_VERSION;

        try {
            final SSLContext sslContext = SSLContext.getInstance(sslVersion);

            new MockUp<HttpsClient>() {

                @Mock
                private SSLContext createSSLContext(
                        HttpsConfigInfoBean configBean) throws GeneralSecurityException, IOException {
                    return sslContext;
                }
            };
        } catch (NoSuchAlgorithmException e) {
            Assert.assertTrue(false);
        }
        HttpsConfigInfoBean oBean = new HttpsConfigInfoBean();
        HttpsClient.getHttpsClient(oBean);
        Assert.assertNotEquals(null, HttpsClient.getHttpsClient(Mockito.mock(HttpsConfigInfoBean.class)));
    }

    @Test
    public void testInitKeyStore(final @Mocked HttpsConfigInfoBean configInfoBean,
            final @Mocked KeyManagerFactory factory) {

        HttpsConfigInfoBean oBean = new HttpsConfigInfoBean();
        new Expectations() {
            {
                configInfoBean.getKeyStorePath();
                result = "/foundation-common/src/test/resources/config/test.1.properties";

                configInfoBean.getKeyStorePasswd();
                result = "1769";

                configInfoBean.getTrustStorePath();
                result = "/foundation-common/src/test/resources/config/test.1.properties";

                configInfoBean.getTrustStorePasswd();
                result = "1769";
            }
        };

        new MockUp<KeyManagerFactory>() {

            @Mock
            public final void init(KeyStore ks, char[] password) {

            }

            @Mock
            public final KeyManager[] getKeyManagers() {
                return null;

            }

        };

        String keyStoreType = KeyStore.getDefaultType();

        try {
            final KeyStore keyStore = KeyStore.getInstance(keyStoreType);

            new MockUp<HttpsClient>() {

                @Mock
                private KeyStore initKeyStore(String storePath, String storePasswd,
                        String storeType) throws IOException {
                    return keyStore;
                }
            };
        } catch (KeyStoreException e) {
            Assert.assertTrue(false);
        }

        HttpsClient.getHttpsClient(oBean);
        Assert.assertNotEquals(null, HttpsClient.getHttpsClient(Mockito.mock(HttpsConfigInfoBean.class)));

    }

}
