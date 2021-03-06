package com.lanjoys.wx.pay;

import com.lanjoys.wx.WxProperty;
import lombok.Data;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import static com.lanjoys.wx.pay.WXPayConstants.USER_AGENT;

@Service
@Data
public class WXPayRequest {
    @Autowired
    WxProperty config;


    /**
     * 请求，只请求一次，不做重试
     *
     * @param domain
     * @param urlSuffix
     * @param data
     * @param connectTimeoutMs
     * @param readTimeoutMs
     * @param useCert          是否使用证书，针对退款、撤销等操作
     * @return
     * @throws Exception
     */
    private String requestOnce(final String domain, String urlSuffix, String data, int connectTimeoutMs, int readTimeoutMs, boolean useCert) throws Exception {
        BasicHttpClientConnectionManager connManager;
        if (useCert) {
            // 证书
            char[] password = config.getPay().getMchId().toCharArray();
            InputStream certStream = config.getPay().getCertStream();
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(certStream, password);

            // 实例化密钥库 & 初始化密钥工厂
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password);

            // 创建 SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    new String[]{"TLSv1"},
                    null,
                    new DefaultHostnameVerifier());

            connManager = new BasicHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.getSocketFactory())
                            .register("https", sslConnectionSocketFactory)
                            .build(),
                    null,
                    null,
                    null
            );
        } else {
            connManager = new BasicHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.getSocketFactory())
                            .register("https", SSLConnectionSocketFactory.getSocketFactory())
                            .build(),
                    null,
                    null,
                    null
            );
        }

        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connManager)
                .build();

        String url = "https://" + domain + urlSuffix;
        HttpPost httpPost = new HttpPost(url);

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(readTimeoutMs).setConnectTimeout(connectTimeoutMs).build();
        httpPost.setConfig(requestConfig);

        StringEntity postEntity = new StringEntity(data, "UTF-8");
        httpPost.addHeader("Content-Type", "text/xml");
        httpPost.addHeader("User-Agent", USER_AGENT + " " + config.getPay().getMchId());
        httpPost.setEntity(postEntity);

        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();
        return EntityUtils.toString(httpEntity, "UTF-8");

    }


    private String request(String urlSuffix, String data, int connectTimeoutMs, int readTimeoutMs, boolean useCert) throws Exception {
        try {
            return requestOnce(config.getPay().getDomain(), urlSuffix, data, connectTimeoutMs, readTimeoutMs, useCert);
        } catch (Exception ex) {
            throw ex;
        }

    }


    /**
     * 可重试的，非双向认证的请求
     *
     * @param urlSuffix
     * @param data
     * @return
     */
    public String requestWithoutCert(String urlSuffix, String data) throws Exception {
        return this.request(urlSuffix, data, config.getPay().getConnectTimeoutMs(), config.getPay().getReadTimeoutMs(), false);
    }

    /**
     * 可重试的，非双向认证的请求
     *
     * @param urlSuffix
     * @param data
     * @param connectTimeoutMs
     * @param readTimeoutMs
     * @return
     */
    public String requestWithoutCert(String urlSuffix, String data, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        return this.request(urlSuffix, data, connectTimeoutMs, readTimeoutMs, false);
    }

    /**
     * 可重试的，双向认证的请求
     *
     * @param urlSuffix
     * @param data
     * @return
     */
    public String requestWithCert(String urlSuffix, String data) throws Exception {
        return this.request(urlSuffix, data, config.getPay().getConnectTimeoutMs(), config.getPay().getReadTimeoutMs(), true);
    }

    /**
     * 可重试的，双向认证的请求
     *
     * @param urlSuffix
     * @param data
     * @param connectTimeoutMs
     * @param readTimeoutMs
     * @return
     */
    public String requestWithCert(String urlSuffix, String data, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        return this.request(urlSuffix, data, connectTimeoutMs, readTimeoutMs, true);
    }
}
