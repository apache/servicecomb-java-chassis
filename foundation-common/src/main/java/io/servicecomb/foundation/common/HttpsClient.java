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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.HttpMethod;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.servicecomb.foundation.common.entities.HttpsConfigInfoBean;
import io.servicecomb.foundation.common.utils.FortifyUtils;

@SuppressWarnings("deprecation")
public final class HttpsClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpsClient.class);

    /**
    * http连接超时时间
    */
    private static final int CONNECTION_TIMEOUT = 10000;

    /**
    * SO超时时间
    */
    private static final int SO_TIMEOUT = 10000;

    /**
    * 端口80
    */
    private static final int PORT_80 = 80;

    /**
    * 端口443
    */
    private static final int PORT_443 = 443;

    private static final String SSL_VERSION = "TLSv1.2";

    private HttpsClient() {
    }

    /**
     * get https connect
     * <功能详细描述>
     * @return HttpClient
     * @see [类、类#方法、类#成员]
     */
    public static HttpClient getHttpsClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), PORT_80));
            registry.register(new Scheme("https", sf, PORT_443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (RuntimeException e) {
            LOGGER.error("Get https client runtime exception: {}", FortifyUtils.getErrorInfo(e));
            return new DefaultHttpClient();
        } catch (Exception e) {
            LOGGER.error("Get https client exception: {}", FortifyUtils.getErrorInfo(e));
            return new DefaultHttpClient();
        }
    }

    /**
     * get https connect
     * <功能详细描述>
     * @return HttpClient
     * @see [类、类#方法、类#成员]
     */
    public static HttpClient getHttpsClient(HttpsConfigInfoBean configBean) {
        try {
            SSLContext sslContext = createSSLContext(configBean);
            SSLSocketFactory sf = new SSLSocketFactory(sslContext);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), PORT_80));
            registry.register(new Scheme("https", sf, PORT_443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (RuntimeException e) {
            LOGGER.error("Get https client runtime exception: {}", FortifyUtils.getErrorInfo(e));
            return new DefaultHttpClient();
        } catch (GeneralSecurityException | IOException e) {
            LOGGER.error("Get https client exception: {}", FortifyUtils.getErrorInfo(e));
            return new DefaultHttpClient();
        }
    }

    /**
     * 创建SSLContext
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws Exception
     */
    private static SSLContext createSSLContext(
            HttpsConfigInfoBean configBean) throws GeneralSecurityException, IOException {
        String sslVersion = SSL_VERSION;
        String keyStoreType = KeyStore.getDefaultType();
        String trustStoreType = KeyStore.getDefaultType();
        String keyStoreAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
        String trustStoreAlgorithm = TrustManagerFactory.getDefaultAlgorithm();

        SSLContext sslContext = null;
        try {
            KeyManagerFactory keyManagerFactory = null;
            String keyStorePath = configBean.getKeyStorePath();
            String keyStorePasswd = configBean.getKeyStorePasswd();
            //            if (!StringUtils.isEmpty(keyStorePath))
            keyStorePasswd = keyStorePasswd == null ? "" : keyStorePasswd;

            keyManagerFactory = KeyManagerFactory.getInstance(keyStoreAlgorithm);
            KeyStore keyStore = initKeyStore(keyStorePath, keyStorePasswd, keyStoreType);
            keyManagerFactory.init(keyStore, keyStorePasswd.toCharArray());

            String trustStorePath = configBean.getTrustStorePath();
            String trustStorePasswd = configBean.getTrustStorePasswd();
            TrustManager[] trustManager = null;
            //            if (!StringUtils.isEmpty(trustStorePath))
            trustStorePasswd = trustStorePasswd == null ? "" : trustStorePasswd;

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(trustStoreAlgorithm);
            KeyStore trustStore = initKeyStore(trustStorePath, trustStorePasswd, trustStoreType);
            trustManagerFactory.init(trustStore);
            trustManager = trustManagerFactory.getTrustManagers();

            sslContext = SSLContext.getInstance(sslVersion);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManager, null);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Unsupported algorithm exception, info: {}", FortifyUtils.getErrorInfo(e));
            throw e;
        } catch (KeyStoreException e) {
            LOGGER.error("Keystore exception,info: {}", FortifyUtils.getErrorInfo(e));
            throw e;
        } catch (GeneralSecurityException e) {
            LOGGER.error("Key management exception,info: {}", FortifyUtils.getErrorInfo(e));
            throw e;
        } catch (IOException e) {
            LOGGER.error("I/O error reading keystore/truststore file, info: {}", FortifyUtils.getErrorInfo(e));
            throw e;
        } catch (Exception e) {
            LOGGER.error("I/O error reading keystore/truststore file, info: {}", FortifyUtils.getErrorInfo(e));
            throw e;
        }
        return sslContext;
    }

    /**
     * 实例化并初始keystore
     * @param keystoreFile
     * @param keyPass
     * @return
     * @throws IOException
     */
    private static KeyStore initKeyStore(String storePath, String storePasswd, String storeType) throws IOException {
        FileInputStream inputStream = null;
        try {
            KeyStore keyStore = KeyStore.getInstance(storeType);
            inputStream = new FileInputStream(new File(storePath));
            keyStore.load(inputStream, storePasswd.toCharArray());
            return keyStore;
        } catch (FileNotFoundException e) {
            throw new IOException("keystore file can not find.");
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Exception trying to load keystore " + storePath + ": " + e.getMessage());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * 执行请求
     * <功能详细描述>
     * @param method 请求方法
     * @param headers 请求头信息
     * @param url 请求URL
     * @param body 请求的数据
     * @return HttpResponse
     * @throws ClientProtocolException ClientProtocolException
     * @throws IOException IOException
     * @see [类、类#方法、类#成员]
     */
    public static HttpResponse execute(String method, Map<String, String> headers, String url,
            String body, HttpsConfigInfoBean configBean) throws ClientProtocolException, IOException {
        if (StringUtils.isEmpty(method)) {
            LOGGER.error("method is valid.");
            return null;
        }
        if (StringUtils.isEmpty(url)) {
            LOGGER.error("url is valid.");
            return null;
        }
        if (headers == null) {
            LOGGER.error("headers is valid.");
            return null;
        }

        HttpClient httpClient = null;
        if (configBean == null) {
            httpClient = getHttpsClient();
        } else {
            httpClient = getHttpsClient(configBean);
        }

        HttpResponse response = null;
        if (HttpMethod.POST.equals(method)) {
            response = post(httpClient, url, body, headers);
        } else if (HttpMethod.GET.equals(method)) {
            response = get(httpClient, url, headers);
        } else if (HttpMethod.DELETE.equals(method)) {
            response = delete(httpClient, url, headers);
        }

        return response;

    }

    /**
     * post请求
     * @param url url
     * @param body 消息体
     * @return HttpResponse 响应结果
     * @throws IOException IO异常
     * @throws ClientProtocolException 客户端协议异常
     */
    private static HttpResponse post(HttpClient httpClient, String url, String body,
            Map<String, String> headers) throws ClientProtocolException, IOException {
        HttpPost postRequest = new HttpPost(url);
        for (Entry<String, String> header : headers.entrySet()) {
            postRequest.addHeader(header.getKey(), header.getValue());
        }

        if (!StringUtils.isEmpty(body)) {
            LOGGER.info("http post body is :{}", body);
            StringWriter writer = new StringWriter();
            writer.write(body);
            StringEntity strEntity = new StringEntity(writer.getBuffer().toString());
            postRequest.setEntity(strEntity);
        }

        return httpClient.execute(postRequest);
    }

    /**
     * http的get请求
     * <功能详细描述>
     * @param url 请求URL
     * @return 响应结果
     * @throws ClientProtocolException
     * @throws IOException
     * @see [类、类#方法、类#成员]
     */
    private static HttpResponse get(HttpClient httpClient, String url,
            Map<String, String> headers) throws ClientProtocolException, IOException {
        HttpGet getRequest = new HttpGet(url);
        for (Entry<String, String> header : headers.entrySet()) {
            getRequest.addHeader(header.getKey(), header.getValue());
        }
        HttpResponse response = httpClient.execute(getRequest);
        return response;
    }

    /**
     * 向rest服务器发起delete请求
     * @param url 请求URL
     * @return 响应结果
     * @throws IOException IO异常
     * @throws ClientProtocolException 客户端协议异常
     * @see [类、类#方法、类#成员]
     */
    private static HttpResponse delete(HttpClient httpClient, String url,
            Map<String, String> headers) throws ClientProtocolException, IOException {
        HttpDelete deleteRequest = new HttpDelete(url);
        for (Entry<String, String> header : headers.entrySet()) {
            deleteRequest.addHeader(header.getKey(), header.getValue());
        }

        return httpClient.execute(deleteRequest);
    }

    private static class MySSLSocketFactory extends SSLSocketFactory {
        private SSLContext sslContext = SSLContext.getInstance(SSL_VERSION);

        MySSLSocketFactory(KeyStore truststore)
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] {tm}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port,
                boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

}
